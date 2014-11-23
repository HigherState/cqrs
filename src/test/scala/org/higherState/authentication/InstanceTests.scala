package org.higherState.authentication

import org.scalatest.{BeforeAndAfter, Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import akka.actor.{Props, ActorSystem}
import org.higherState.cqrs._
import scala.collection.mutable
import akka.util.Timeout
import org.higherState.repository._
import java.util.concurrent.atomic.AtomicReference

import org.higherState.repository.HashMapRepository
import scala.concurrent.{Future, ExecutionContext}
import org.higherState.cqrs.akkaPattern.{ActorDispatcherFactory, ActorAdapter}
import scalaz.{~>, Monad}

class AuthenticationCommandHandlerImpl[In[+_],Out[+_]]
  (val maxNumberOfTries:Int, val repository:KeyValueRepository[In, UserLogin, UserCredentials])
  (implicit val pipe: ~>[In,Out], val fm:Monad[Out] with Failure[Out])
  extends AuthenticationCommandHandler[In, Out] with MonadicFailureDirectives[Out]
class AuthenticationQueryExecutorImpl[In[+_],Out[+_]]
  (val repository:KeyValueRepository[In, UserLogin, UserCredentials])
  (implicit val pipe: ~>[In,Out], val fm:Monad[Out] with Failure[Out])
  extends AuthenticationQueryExecutor[In, Out] with MonadicFailureDirectives[Out]

class AuthenticationCommandHandlerActorImpl[In[+_],Out[+_]]
  (val maxNumberOfTries:Int, val repository:KeyValueRepository[In, UserLogin, UserCredentials])
  (implicit val pipe: ~>[In,Out], val fm:Monad[Out] with Failure[Out], val executionContext:ExecutionContext)
  extends AuthenticationCommandHandler[In, Out] with MonadicFailureDirectives[Out] with ActorAdapter
class AuthenticationQueryExecutorActorImpl[In[+_],Out[+_]]
  (val repository:KeyValueRepository[In, UserLogin, UserCredentials])
  (implicit val pipe: ~>[In,Out], val fm:Monad[Out with Failure[Out]], val executionContext:ExecutionContext)
  extends AuthenticationQueryExecutor[In, Out] with MonadicFailureDirectives[Out] with ActorAdapter

class InMemoryKeyValueCommandHandlerActorImpl[Out[+_], Key, Value]
  (val state:AtomicReference[Map[Key, Value]])
  (implicit val fm:Monad[Out], val executionContext:ExecutionContext)
  extends InMemoryKeyValueCommandHandler[Out, Key, Value] with MonadicDirectives[Out] with ActorAdapter
class InMemoryKeyValueQueryExecutorActorImpl[Out[+_], Key, Value]
  (val state:AtomicReference[Map[Key, Value]])
  (implicit val fm:Monad[Out], val executionContext:ExecutionContext)
  extends InMemoryKeyValueQueryExecutor[Out, Key, Value] with MonadicDirectives[Out] with ActorAdapter

class InstanceTests extends FunSuite with Matchers with ScalaFutures with BeforeAndAfter {

  import scala.concurrent.duration._
  import NaturalTransforms._
  import Monads._

  implicit val system = ActorSystem("System")
  implicit val exectionContext:ExecutionContext = system.dispatcher
  implicit val globalTimeout:Timeout = 5.minutes

  test("Simple service, not designed to handle concurrency") {
    //simple repository service
    val testHashRepository =
      new HashMapRepository(mutable.HashMap.empty[UserLogin, UserCredentials])

    //Identity authentication service
    val ch = new AuthenticationCommandHandlerImpl[Identity, Valid](10, testHashRepository){}
    val qe = new AuthenticationQueryExecutorImpl[Identity, Valid](testHashRepository){}
    val testAuthenticationService = new AuthenticationService(Dispatcher(ch, qe))

    testAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Success())
    testAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Success(UserLogin("test@test.com")))
    testAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Failure(scalaz.NonEmptyList(UserCredentialsAlreadyExistFailure(UserLogin("test@test.com")))))
  }

  test("CQRS actor implementation on repository with a future handling on the authentication service") {
    //CQRS actors repository service.
    //Single command handler actor and single query executor actor
    val atomicHashMap = new AtomicReference[Map[UserLogin, UserCredentials]](Map.empty[UserLogin, UserCredentials])
    val repositoryCommandHandler = system.actorOf(Props(new InMemoryKeyValueCommandHandlerActorImpl[Identity, UserLogin, UserCredentials](atomicHashMap)))
    val repositoryQueryExecutor = system.actorOf(Props(new InMemoryKeyValueQueryExecutorActorImpl[Identity, UserLogin, UserCredentials](atomicHashMap)))
    val akkaCqrsRepository = new KeyValueCqrsRepository[Future, UserLogin, UserCredentials](ActorDispatcherFactory.future(repositoryCommandHandler, repositoryQueryExecutor))

    val authCommandHandler = new AuthenticationCommandHandlerImpl[Future, FutureValid](10, akkaCqrsRepository)
    val authQueryExecutor = new AuthenticationQueryExecutorImpl[Future, FutureValid](akkaCqrsRepository)
    val futureAuthenticationService = new AuthenticationService[FutureValid](Dispatcher(authCommandHandler, authQueryExecutor))

    //Authentication service will handle futures from repository actor

    whenReady(futureAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result1 =>
      result1 should equal (scalaz.Success())
      whenReady(futureAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password"))) {result2 =>
        result2 should equal (scalaz.Success(UserLogin("test@test.com")))
      }
      whenReady(futureAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result3 =>
        result3 should equal (scalaz.Failure(scalaz.NonEmptyList(UserCredentialsAlreadyExistFailure(UserLogin("test@test.com")))))
      }
    }
  }



  test("CQRS actor authentication service using the simple hashmap repository") {
    //simple repository service
    val testHashRepository =
      new HashMapRepository(mutable.HashMap.empty[UserLogin, UserCredentials])

    val authCommandHandler = system.actorOf(Props(new AuthenticationCommandHandlerActorImpl[Identity, FutureValid](10, testHashRepository)))
    val authQueryExecutor = system.actorOf(Props(new AuthenticationQueryExecutorActorImpl[Identity, FutureValid](testHashRepository)))
    val akkaCqrsAuthenticationService = new AuthenticationService[FutureValid](ActorDispatcherFactory.futureValid(authCommandHandler, authQueryExecutor))

    whenReady(akkaCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result1 =>
      result1 should equal (scalaz.Success())
      whenReady(akkaCqrsAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password"))) {result2 =>
        result2 should equal (scalaz.Success(UserLogin("test@test.com")))
      }
      whenReady(akkaCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result3 =>
        result3 should equal (scalaz.Failure(scalaz.NonEmptyList(UserCredentialsAlreadyExistFailure(UserLogin("test@test.com")))))
      }
    }
  }

  test("CQRS actor authentication piping from actor repository service") {
    val atomicHashMap = new AtomicReference[Map[UserLogin, UserCredentials]](Map.empty[UserLogin, UserCredentials])
    val repositoryCommandHandler = system.actorOf(Props(new InMemoryKeyValueCommandHandlerActorImpl[Identity, UserLogin, UserCredentials](atomicHashMap)))
    val repositoryQueryExecutor = system.actorOf(Props(new InMemoryKeyValueQueryExecutorActorImpl[Identity, UserLogin, UserCredentials](atomicHashMap)))
    val akkaCqrsRepository = new KeyValueCqrsRepository[Future, UserLogin, UserCredentials](ActorDispatcherFactory.future(repositoryCommandHandler, repositoryQueryExecutor))

    val authCommandHandler = system.actorOf(Props(new AuthenticationCommandHandlerActorImpl[Future, FutureValid](10, akkaCqrsRepository)))
    val authQueryExecutor = system.actorOf(Props(new AuthenticationQueryExecutorActorImpl[Future, FutureValid](akkaCqrsRepository)))
    val akkaChainedCqrsAuthenticationService = new AuthenticationService[FutureValid](ActorDispatcherFactory.futureValid(authCommandHandler, authQueryExecutor))

    whenReady(akkaChainedCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result1 =>
      result1 should equal (scalaz.Success())
      whenReady(akkaChainedCqrsAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password"))) {result2 =>
        result2 should equal (scalaz.Success(UserLogin("test@test.com")))
      }
      whenReady(akkaChainedCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result3 =>
        result3 should equal (scalaz.Failure(scalaz.NonEmptyList(UserCredentialsAlreadyExistFailure(UserLogin("test@test.com")))))
      }
    }
  }
}