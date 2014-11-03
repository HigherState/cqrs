package org.higherState.authentication

import org.scalatest.{BeforeAndAfter, Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import akka.actor.{ActorRef, ActorSystem}
import org.higherState.cqrs._
import org.higherState.cqrs.Output.Valid
import scala.collection.mutable
import akka.util.Timeout
import org.higherState.repository._
import java.util.concurrent.atomic.AtomicReference
import org.higherState.cqrs.Pipe
import org.higherState.repository.HashMapRepository
import scala.concurrent.ExecutionContext

class InstanceTests extends FunSuite with Matchers with ScalaFutures with BeforeAndAfter {

  import scala.concurrent.duration._
  import Pipes._

  implicit val system = ActorSystem("System")
  implicit val exectionContext:ExecutionContext = system.dispatcher
  implicit val globalTimeout:Timeout = 5.minutes

  test("Simple service, not designed to handle concurrency") {
    //simple repository service
    val testHashRepository =
      HashMapRepository(mutable.HashMap.empty[UserLogin, UserCredentials])

    //Identity authentication service
    val testAuthenticationService =
      new AuthenticationService with ValidationCqrs {
        //Both queryExecutor and commandHandler can be combined for simplicity
        val commandAndQuery =
          new AuthenticationCommandHandler with AuthenticationQueryExecutor with ValidationDirectives {
            val maxNumberOfTries: Int = 10
            val repository: Pipe[KeyValueRepository[UserLogin, UserCredentials]] =
              Pipe(testHashRepository)
          }

        def commandHandler: CommandHandler[C] with Valid =
          commandAndQuery

        def queryExecutor: QueryExecutor[QP] with Valid =
          commandAndQuery
      }

    testAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Success())
    testAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Success(UserLogin("test@test.com")))
  }

  test("CQRS actor implementation on repository with a future handling on the authentication service") {

    //CQRS actors repository service.
    //Single command handler actor and single query executor actor
    val atomicHashMap = new AtomicReference[Map[UserLogin, UserCredentials]](Map.empty[UserLogin, UserCredentials])
    val akkaCqrsRepository =
      new impl.ActorCqrs("AkkaCqrsRepository") with KeyValueCqrsRepository[UserLogin, UserCredentials] {
        val queryExecutor: ActorRef =
        //here actor can be enriched with supervisor/routing etc
          getQueryRef {
            new impl.ActorAdapter()(this.executionContext) with InMemoryKeyValueQueryExecutor[UserLogin, UserCredentials] with IdentityDirectives {
              def state = atomicHashMap
            }
          }

        val commandHandler: ActorRef =
        //here actor can be enriched with supervisor/routing etc
          getCommandHandlerRef {
            new impl.ActorAdapter()(this.executionContext) with InMemoryKeyValueCommandHandler[UserLogin, UserCredentials] with IdentityDirectives {
              def state = atomicHashMap
            }
          }
      }


    //Authentication service will handle futures from repository actor
    val futureAuthenticationService =
      new impl.FutureValidationCqrs with AuthenticationService {

        val commandAndQuery =
          new impl.FutureValidationDirectives()(this.executionContext) with AuthenticationCommandHandler with AuthenticationQueryExecutor {
            val maxNumberOfTries: Int = 10
            val repository: Pipe[KeyValueRepository[UserLogin, UserCredentials] with Output.Future] =
              Pipe(akkaCqrsRepository)
          }

        def queryExecutor: QueryExecutor[QP] with Output.FutureValid =
          commandAndQuery

        def commandHandler: CommandHandler[C] with Output.FutureValid =
          commandAndQuery
      }

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
      HashMapRepository(mutable.HashMap.empty[UserLogin, UserCredentials])

    val akkaCqrsAuthenticationService =
      new impl.ActorValidationCqrs("AkkaCqrsAuthenticationService") with AuthenticationService {

        val queryExecutor: ActorRef =
          getQueryRef {
            new impl.ActorAdapter()(this.executionContext) with AuthenticationQueryExecutor with ValidationDirectives {
              val repository:Pipe[KeyValueRepository[UserLogin, UserCredentials]] = Pipe(testHashRepository)
            }
          }

        val commandHandler: ActorRef =
          getCommandHandlerRef {
            new impl.ActorAdapter()(this.executionContext) with AuthenticationCommandHandler with ValidationDirectives {
              val maxNumberOfTries: Int = 10
              val repository:Pipe[KeyValueRepository[UserLogin, UserCredentials]] = Pipe(testHashRepository)
            }
          }
      }

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
    val akkaCqrsRepository =
      new impl.ActorCqrs("AkkaCqrsRepository2") with KeyValueCqrsRepository[UserLogin, UserCredentials] {
        val queryExecutor: ActorRef =
        //here actor can be enriched with supervisor/routing etc
          getQueryRef {
            new impl.ActorAdapter()(this.executionContext) with InMemoryKeyValueQueryExecutor[UserLogin, UserCredentials] with IdentityDirectives {
              def state = atomicHashMap
            }
          }

        val commandHandler: ActorRef =
        //here actor can be enriched with supervisor/routing etc
          getCommandHandlerRef {
            new impl.ActorAdapter()(this.executionContext) with InMemoryKeyValueCommandHandler[UserLogin, UserCredentials] with IdentityDirectives {
              def state = atomicHashMap
            }
          }
      }

    val akkaChainedCqrsAuthenticationService =
      new impl.ActorValidationCqrs("AkkaChainedCqrsAuthenticationService") with AuthenticationService {

        val queryExecutor: ActorRef =
          getQueryRef {
            new impl.ActorAdapter()(this.executionContext) with AuthenticationQueryExecutor with FutureValidationDirectives {
              val repository: Pipe[KeyValueRepository[UserLogin, UserCredentials]] = Pipe(akkaCqrsRepository)
            }
          }

        val commandHandler: ActorRef =
          getCommandHandlerRef {
            new impl.ActorAdapter()(this.executionContext) with AuthenticationCommandHandler with FutureValidationDirectives {
              val maxNumberOfTries: Int = 10
              val repository: Pipe[KeyValueRepository[UserLogin, UserCredentials]] = Pipe(akkaCqrsRepository)
            }
          }
      }

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