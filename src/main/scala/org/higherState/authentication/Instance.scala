package org.higherState.authentication

import akka.actor.{ActorRef, ActorSystem}
import org.higherState.cqrs.{ValidationDirectives, CommandHandler, QueryExecutor, ValidationCqrs, Pipe, Pipes}
import org.higherState.cqrs.Output.Valid
import scala.collection.mutable
import org.higherState.cqrs.akka.{ActorAdapter, AkkaValidationCqrs}
import akka.util.Timeout

object Instance {

  import scala.concurrent.duration._
  import Pipes._

  implicit val system = ActorSystem("System")
  implicit val excon = system.dispatcher
  implicit val globalTimeout:Timeout = 5.minutes


  val identityRepository =
    new InMemoryAuthenticationRepository {
      val state: mutable.Map[UserLogin, (UserCredentials, Int)] = mutable.Map.empty
    }

  //Unprotected identity service
  val identityService = new AuthenticationService with ValidationCqrs {

    val commandAndQuery =
      new AuthenticationCommandHandler with AuthenticationQueryExecutor with ValidationDirectives {
        val maxNumberOfTries: Int = 10
        val repository: Pipe[AuthenticationRepository] =
          Pipe(identityRepository)
      }

    def commandHandler: CommandHandler[C] with Valid =
      commandAndQuery

    def queryExecutor: QueryExecutor[QP] with Valid =
      commandAndQuery
  }


  //akka identity validation service
  val akkaService = new AkkaValidationCqrs with AuthenticationService {

    val commandHandler: ActorRef =
      getCommandHandlerRef("authenticationService"){
        new ActorAdapter with AuthenticationCommandHandler with ValidationDirectives {
          val maxNumberOfTries: Int = 10
          val repository: Pipe[AuthenticationRepository] = Pipe(identityRepository)
        }
      }

    val queryExecutor: ActorRef =
      getQueryRef("authenticationService") {
        new ActorAdapter with AuthenticationQueryExecutor with ValidationDirectives {
          val repository: Pipe[AuthenticationRepository] = Pipe(identityRepository)
        }
      }
  }
}
