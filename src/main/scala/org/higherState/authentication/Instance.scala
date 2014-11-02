package org.higherState.authentication

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

object Instance {

  import scala.concurrent.duration._
  import Pipes._

  implicit val system = ActorSystem("System")
  implicit val exectionContext:ExecutionContext = system.dispatcher
  implicit val globalTimeout:Timeout = 5.minutes


  //
  // Test service, not designed to handle concurrency
  //

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
          val repository = Pipe(testHashRepository)
        }

      def commandHandler: CommandHandler[C] with Valid =
        commandAndQuery

      def queryExecutor: QueryExecutor[QP] with Valid =
        commandAndQuery
    }



  //
  //CQRS actor implementation on repository with a future handling on the authentication service
  //

  //CQRS actors repository service.
  //Single command handler actor and single query executor actor
  val atomicHashMap = new AtomicReference[Map[UserLogin, UserCredentials]](Map.empty[UserLogin, UserCredentials])
  val akkaCqrsRepository =
    new impl.ActorCqrs("AkkaCqrsRepository") with KeyValueCqrsRepository[UserLogin, UserCredentials]  {
      val queryExecutor: ActorRef =
        getQueryRef {
          //within here actor can be enriched with supervisor/routing etc
          new impl.ActorAdapter with InMemoryKeyValueQueryExecutor[UserLogin, UserCredentials] with IdentityDirectives {
            def state = atomicHashMap
          }
        }

      val commandHandler: ActorRef =
        getCommandHandlerRef {
          //within here actor can be enriched with supervisor/routing etc
          new impl.ActorAdapter with InMemoryKeyValueCommandHandler[UserLogin, UserCredentials] with IdentityDirectives {
            def state = atomicHashMap
          }
        }
    }

  //Authentication service will handle futures from repository actor
  val futureAuthenticationService =
    new impl.FutureValidationCqrs with AuthenticationService {

      val commandAndQuery =
        new impl.FutureValidationDirectives with AuthenticationCommandHandler with AuthenticationQueryExecutor {
          val maxNumberOfTries: Int = 10
          val repository = Pipe(akkaCqrsRepository)
        }

      def queryExecutor: QueryExecutor[QP] with Output.FutureValid =
        commandAndQuery

      def commandHandler: CommandHandler[C] with Output.FutureValid =
        commandAndQuery
    }


  //
  //  CQRS actor authentication service using the simple hashmap repository
  //
  val akkaCqrsAuthenticationService =
    new impl.ActorCqrs("AkkaCqrsAuthenticationService") with AuthenticationService {

      val queryExecutor: ActorRef =
        getQueryRef {
          //within here actor can be enriched with supervisor/routing etc
          new impl.ActorAdapter with AuthenticationQueryExecutor with ValidationDirectives {
            val repository = Pipe(testHashRepository)
          }
        }

      val commandHandler: ActorRef =
        getCommandHandlerRef {
          //within here actor can be enriched with supervisor/routing etc
          new impl.ActorAdapter with AuthenticationCommandHandler with ValidationDirectives {
            val maxNumberOfTries: Int = 10
            val repository = Pipe(testHashRepository)
          }
        }
    }


  //
  // CQRS actor authentication piping from actor repository service
  //
  val akkaChainedCqrsAuthenticationService =
    new impl.ActorCqrs("AkkaChainedCqrsAuthenticationService") with AuthenticationService {

      val queryExecutor: ActorRef =
        getQueryRef {
          //within here actor can be enriched with supervisor/routing etc
          new impl.ActorAdapter with AuthenticationQueryExecutor with FutureValidationDirectives {
            val repository = Pipe(akkaCqrsRepository)
          }
        }

      val commandHandler: ActorRef =
        getCommandHandlerRef {
          //within here actor can be enriched with supervisor/routing etc
          new impl.ActorAdapter with AuthenticationCommandHandler with FutureValidationDirectives {
            val maxNumberOfTries: Int = 10
            val repository = Pipe(akkaCqrsRepository)
          }
        }
    }
}
