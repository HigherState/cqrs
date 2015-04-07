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
import scalaz.{~>, Monad}
import org.higherState.cqrs.std._

class AuthenticationCommandHandlerImpl[In[+_],Out[+_]]
  (val maxNumberOfTries:Int, val repository:KeyValueRepository[In, UserLogin, UserCredentials])
  (implicit val pipe: ~>[In,Out], val monad:FMonad[ValidationFailure, Out])
  extends AuthenticationCommandHandler[In, Out] with FMonadBind[ValidationFailure, Out]

class AuthenticationQueryExecutorImpl[In[+_],Out[+_]]
  (val repository:KeyValueRepository[In, UserLogin, UserCredentials])
  (implicit val pipe: ~>[In,Out], val monad:FMonad[ValidationFailure,Out])
  extends AuthenticationQueryExecutor[In, Out] with FMonadBind[ValidationFailure,Out]

class AuthenticationCommandHandlerActorImpl[In[+_],Out[+_]]
  (val maxNumberOfTries:Int, val repository:KeyValueRepository[In, UserLogin, UserCredentials])
  (implicit val pipe: ~>[In,Out], val monad:FMonad[ValidationFailure, Out])
  extends AuthenticationCommandHandler[In, Out] with FMonadBind[ValidationFailure, Out] with ActorAdapter
class AuthenticationQueryExecutorActorImpl[In[+_],Out[+_]]
  (val repository:KeyValueRepository[In, UserLogin, UserCredentials])
  (implicit val pipe: ~>[In,Out], val monad:FMonad[ValidationFailure, Out])
  extends AuthenticationQueryExecutor[In, Out] with FMonadBind[ValidationFailure, Out] with ActorAdapter

class InMemoryKeyValueCommandHandlerActorImpl[Out[+_]:Monad, Key, Value]
  (val state:AtomicReference[Map[Key, Value]])
  extends InMemoryKeyValueCommandHandler[Out, Key, Value] with ActorAdapter
class InMemoryKeyValueQueryExecutorActorImpl[Out[+_]:Monad, Key, Value]
  (val state:AtomicReference[Map[Key, Value]])
  extends InMemoryKeyValueQueryExecutor[Out, Key, Value] with ActorAdapter

class InstanceTests extends FunSuite with Matchers with ScalaFutures with BeforeAndAfter {

  import scala.concurrent.duration._
  import Transforms._
  import IdMonad._

  implicit val system = ActorSystem("System")
  implicit val exectionContext:ExecutionContext = system.dispatcher
  implicit val globalTimeout:Timeout = 5.minutes

  type FV[+T] = FutureValid[ValidationFailure, T]
  type V[+T] = Valid[ValidationFailure, T]

  test("Simple service, not designed to handle concurrency") {
    //simple repository service
    val testHashRepository =
      new HashMapRepository(mutable.HashMap.empty[UserLogin, UserCredentials])

    //Identity authentication service
    val ch = new AuthenticationCommandHandlerImpl[Id, V](10, testHashRepository){}
    val qe = new AuthenticationQueryExecutorImpl[Id, V](testHashRepository){}
    val testAuthenticationService = new AuthenticationService(CommandQueryController(ch, qe))

    testAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Success())
    testAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Success(UserLogin("test@test.com")))
    testAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Failure(scalaz.NonEmptyList(UserCredentialsAlreadyExistFailure(UserLogin("test@test.com")))))
  }

  test("CQRS actor implementation on repository with a future handling on the authentication service") {
    //CQRS actors repository service.
    //Single command handler actor and single query executor actor
    val atomicHashMap = new AtomicReference[Map[UserLogin, UserCredentials]](Map.empty[UserLogin, UserCredentials])
    val repositoryCommandHandler = system.actorOf(Props(new InMemoryKeyValueCommandHandlerActorImpl[Id, UserLogin, UserCredentials](atomicHashMap)))
    val repositoryQueryExecutor = system.actorOf(Props(new InMemoryKeyValueQueryExecutorActorImpl[Id, UserLogin, UserCredentials](atomicHashMap)))
    val akkaCqrsRepository = new KeyValueCqrsRepository[Future, UserLogin, UserCredentials](CommandQueryController.actor[Id](repositoryCommandHandler, repositoryQueryExecutor))

    val authCommandHandler = new AuthenticationCommandHandlerImpl[Future, FV](10, akkaCqrsRepository)
    val authQueryExecutor = new AuthenticationQueryExecutorImpl[Future, FV](akkaCqrsRepository)
    val futureAuthenticationService = new AuthenticationService[FV](CommandQueryController(authCommandHandler, authQueryExecutor))

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

    val authCommandHandler = system.actorOf(Props(new AuthenticationCommandHandlerActorImpl[Id, FV](10, testHashRepository)))
    val authQueryExecutor = system.actorOf(Props(new AuthenticationQueryExecutorActorImpl[Id, FV](testHashRepository)))
    val akkaCqrsAuthenticationService = new AuthenticationService[FV](CommandQueryController.actor[V](authCommandHandler, authQueryExecutor))

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
    val repositoryCommandHandler = system.actorOf(Props(new InMemoryKeyValueCommandHandlerActorImpl[Id, UserLogin, UserCredentials](atomicHashMap)))
    val repositoryQueryExecutor = system.actorOf(Props(new InMemoryKeyValueQueryExecutorActorImpl[Id, UserLogin, UserCredentials](atomicHashMap)))
    val akkaCqrsRepository = new KeyValueCqrsRepository[Future, UserLogin, UserCredentials](CommandQueryController.actor[Id](repositoryCommandHandler, repositoryQueryExecutor))

    val authCommandHandler = system.actorOf(Props(new AuthenticationCommandHandlerActorImpl[Future, FV](10, akkaCqrsRepository)))
    val authQueryExecutor = system.actorOf(Props(new AuthenticationQueryExecutorActorImpl[Future, FV](akkaCqrsRepository)))
    val akkaChainedCqrsAuthenticationService = new AuthenticationService[FV](CommandQueryController.actor[V](authCommandHandler, authQueryExecutor))

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
