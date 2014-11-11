package org.higherState.authentication

import org.scalatest.{BeforeAndAfter, Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import akka.actor.{Props, ActorRef, ActorSystem}
import org.higherState.cqrs._
import scala.collection.mutable
import akka.util.Timeout
import org.higherState.repository._
import java.util.concurrent.atomic.AtomicReference
import org.higherState.cqrs.Pipe
import org.higherState.repository.HashMapRepository
import scala.concurrent.{Future, ExecutionContext}
import org.higherState.cqrs.akkaPattern.{ActorDispatcherFactory, ActorAdapter}
import scalaz.Monad

class AuthenticationCommandHandlerImpl[In[+_],Out[+_]](val maxNumberOfTries:Int, val repository:KeyValueRepository[In, UserLogin, UserCredentials])(implicit val pipe:Pipe[In,Out], val fm:Monad[Out]) extends AuthenticationCommandHandler[In, Out] with MonadicDirectives[Out]
class AuthenticationQueryExecutorImpl[In[+_],Out[+_]](val repository:KeyValueRepository[In, UserLogin, UserCredentials])(implicit val pipe:Pipe[In,Out], val fm:Monad[Out]) extends AuthenticationQueryExecutor[In, Out] with MonadicDirectives[Out]

class InMemoryKeyValueCommandHandlerActorImpl[Out[+_], Key, Value](val state:AtomicReference[Map[Key, Value]])(implicit val fm:Monad[Out], val executionContext:ExecutionContext) extends InMemoryKeyValueCommandHandler[Out, Key, Value] with MonadicDirectives[Out] with ActorAdapter
class InMemoryKeyValueQueryExecutorActorImpl[Out[+_], Key, Value](val state:AtomicReference[Map[Key, Value]])(implicit val fm:Monad[Out], val executionContext:ExecutionContext) extends InMemoryKeyValueQueryExecutor[Out, Key, Value] with MonadicDirectives[Out] with ActorAdapter

class InstanceTests extends FunSuite with Matchers with ScalaFutures with BeforeAndAfter {

  import scala.concurrent.duration._
  import Pipes._
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
    val testAuthenticationService =
      new AuthenticationService(Dispatcher(ch, qe))

    testAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Success())
    testAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Success(UserLogin("test@test.com")))
  }

  test("CQRS actor implementation on repository with a future handling on the authentication service") {

    //CQRS actors repository service.
    //Single command handler actor and single query executor actor
    val atomicHashMap = new AtomicReference[Map[UserLogin, UserCredentials]](Map.empty[UserLogin, UserCredentials])
    val rch = system.actorOf(Props(new InMemoryKeyValueCommandHandlerActorImpl[Identity, UserLogin, UserCredentials](atomicHashMap)))
    val rqe = system.actorOf(Props(new InMemoryKeyValueCommandHandlerActorImpl[Identity, UserLogin, UserCredentials](atomicHashMap)))
    val akkaCqrsRepository = new KeyValueCqrsRepository[Future, UserLogin, UserCredentials](ActorDispatcherFactory.future(rch, rqe))


    val ach = new AuthenticationCommandHandlerImpl[Future, FutureValid](10, akkaCqrsRepository)
    val aqe = new AuthenticationQueryExecutorImpl[Future, FutureValid](akkaCqrsRepository)
    val futureAuthenticationService = new AuthenticationService[FutureValid](Dispatcher(ach, aqe))

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

//
//
//  test("CQRS actor authentication service using the simple hashmap repository") {
//    //simple repository service
//    val testHashRepository =
//      HashMapRepository(mutable.HashMap.empty[UserLogin, UserCredentials])
//
//    val akkaCqrsAuthenticationService =
//      new impl.ActorValidationCqrs("AkkaCqrsAuthenticationService") with AuthenticationService {
//
//        val queryExecutor: ActorRef =
//          getQueryRef {
//            new impl.ActorAdapter()(this.executionContext) with AuthenticationQueryExecutor with ValidationDirectives {
//              val repository:Pipe[KeyValueRepository[UserLogin, UserCredentials]] = Pipe(testHashRepository)
//            }
//          }
//
//        val commandHandler: ActorRef =
//          getCommandHandlerRef {
//            new impl.ActorAdapter()(this.executionContext) with AuthenticationCommandHandler with ValidationDirectives {
//              val maxNumberOfTries: Int = 10
//              val repository:Pipe[KeyValueRepository[UserLogin, UserCredentials]] = Pipe(testHashRepository)
//            }
//          }
//      }
//
//    whenReady(akkaCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result1 =>
//      result1 should equal (scalaz.Success())
//      whenReady(akkaCqrsAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password"))) {result2 =>
//        result2 should equal (scalaz.Success(UserLogin("test@test.com")))
//      }
//      whenReady(akkaCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result3 =>
//        result3 should equal (scalaz.Failure(scalaz.NonEmptyList(UserCredentialsAlreadyExistFailure(UserLogin("test@test.com")))))
//      }
//    }
//  }
//
//  test("CQRS actor authentication piping from actor repository service") {
//    val atomicHashMap = new AtomicReference[Map[UserLogin, UserCredentials]](Map.empty[UserLogin, UserCredentials])
//    val akkaCqrsRepository =
//      new impl.ActorCqrs("AkkaCqrsRepository2") with KeyValueCqrsRepository[UserLogin, UserCredentials] {
//        val queryExecutor: ActorRef =
//        //here actor can be enriched with supervisor/routing etc
//          getQueryRef {
//            new impl.ActorAdapter()(this.executionContext) with InMemoryKeyValueQueryExecutor[UserLogin, UserCredentials] with IdentityDirectives {
//              def state = atomicHashMap
//            }
//          }
//
//        val commandHandler: ActorRef =
//        //here actor can be enriched with supervisor/routing etc
//          getCommandHandlerRef {
//            new impl.ActorAdapter()(this.executionContext) with InMemoryKeyValueCommandHandler[UserLogin, UserCredentials] with IdentityDirectives {
//              def state = atomicHashMap
//            }
//          }
//      }
//
//    val akkaChainedCqrsAuthenticationService =
//      new impl.ActorValidationCqrs("AkkaChainedCqrsAuthenticationService") with AuthenticationService {
//
//        val queryExecutor: ActorRef =
//          getQueryRef {
//            new impl.ActorAdapter()(this.executionContext) with AuthenticationQueryExecutor with FutureValidationDirectives {
//              val repository: Pipe[KeyValueRepository[UserLogin, UserCredentials]] = Pipe(akkaCqrsRepository)
//            }
//          }
//
//        val commandHandler: ActorRef =
//          getCommandHandlerRef {
//            new impl.ActorAdapter()(this.executionContext) with AuthenticationCommandHandler with FutureValidationDirectives {
//              val maxNumberOfTries: Int = 10
//              val repository: Pipe[KeyValueRepository[UserLogin, UserCredentials]] = Pipe(akkaCqrsRepository)
//            }
//          }
//      }
//
//    whenReady(akkaChainedCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result1 =>
//      result1 should equal (scalaz.Success())
//      whenReady(akkaChainedCqrsAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password"))) {result2 =>
//        result2 should equal (scalaz.Success(UserLogin("test@test.com")))
//      }
//      whenReady(akkaChainedCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result3 =>
//        result3 should equal (scalaz.Failure(scalaz.NonEmptyList(UserCredentialsAlreadyExistFailure(UserLogin("test@test.com")))))
//      }
//    }
//  }
}