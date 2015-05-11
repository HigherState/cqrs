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

class InstanceTests extends FunSuite with Matchers with ScalaFutures with BeforeAndAfter {

  import scala.concurrent.duration._
  import Transforms._
  import IdMonad._

  implicit val system = ActorSystem("System")
  implicit val exectionContext:ExecutionContext = system.dispatcher
  implicit val globalTimeout:Timeout = 5.minutes

  type FV[+T] = FutureValid[String, T]
  type V[+T] = Valid[String, T]

  test("Simple service, not designed to handle concurrency") {
    //simple repository service
    val testHashRepository =
      new HashMapRepository(mutable.HashMap.empty[UserLogin, UserCredentials])

    //Identity authentication service
    val ch = new AuthenticationCommandHandler[V, Id](testHashRepository, 10)
    val qe = new AuthenticationQueryExecutor[V, Id](testHashRepository)
    val testAuthenticationService = new AuthenticationService(CommandQueryController(ch, qe))

    testAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Success(Acknowledged))
    testAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Success(UserLogin("test@test.com")))
    testAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password")) should equal (scalaz.Failure(scalaz.NonEmptyList("UserCredentialsAlreadyExistFailure(userLogin)")))
  }

  test("CQRS actor implementation on repository with a future handling on the authentication service") {
    //CQRS actors repository service.
    //Single command handler actor and single query executor actor
    val atomicHashMap = new AtomicReference[Map[UserLogin, UserCredentials]](Map.empty[UserLogin, UserCredentials])
    val repositoryCommandHandler = system.actorOf(Props(AsActor(new InMemoryKeyValueCommandHandler[Id, UserLogin, UserCredentials](atomicHashMap))))
    val repositoryQueryExecutor = system.actorOf(Props(AsActor[Id,Kvqe[UserLogin, UserCredentials]#I](new InMemoryKeyValueQueryExecutor[Id, UserLogin, UserCredentials](atomicHashMap))))
    val akkaCqrsRepository =
      new KeyValueCqrsRepository[Future, UserLogin, UserCredentials](
        CommandQueryController.actor[Id][KeyValueCommand[UserLogin, UserCredentials], Kvqe[UserLogin, UserCredentials]#I](repositoryCommandHandler, repositoryQueryExecutor))

    val authCommandHandler = new AuthenticationCommandHandler[FV, Future](akkaCqrsRepository, 10)
    val authQueryExecutor = new AuthenticationQueryExecutor[FV, Future](akkaCqrsRepository)
    val futureAuthenticationService = new AuthenticationService[FV](CommandQueryController(authCommandHandler, authQueryExecutor))

    //Authentication service will handle futures from repository actor

    whenReady(futureAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result1 =>
      result1 should equal (scalaz.Success(Acknowledged))
      whenReady(futureAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password"))) {result2 =>
        result2 should equal (scalaz.Success(UserLogin("test@test.com")))
      }
      whenReady(futureAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result3 =>
        result3 should equal (scalaz.Failure(scalaz.NonEmptyList("UserCredentialsAlreadyExistFailure(userLogin)")))
      }
    }
  }


  test("CQRS actor authentication service using the simple hashmap repository") {
    //simple repository service
    val testHashRepository =
      new HashMapRepository(mutable.HashMap.empty[UserLogin, UserCredentials])

    val authCommandHandler = system.actorOf(Props(AsActor(new AuthenticationCommandHandler[V, Id](testHashRepository, 10))))
    val authQueryExecutor = system.actorOf(Props(AsActor(new AuthenticationQueryExecutor[V, Id](testHashRepository))))
    val akkaCqrsAuthenticationService = new AuthenticationService[FV](CommandQueryController.actor[V](authCommandHandler, authQueryExecutor))

    whenReady(akkaCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result1 =>
      result1 should equal (scalaz.Success(Acknowledged))
      whenReady(akkaCqrsAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password"))) {result2 =>
        result2 should equal (scalaz.Success(UserLogin("test@test.com")))
      }
      whenReady(akkaCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result3 =>
        result3 should equal (scalaz.Failure(scalaz.NonEmptyList("UserCredentialsAlreadyExistFailure(userLogin)")))
      }
    }
  }

  test("CQRS actor authentication piping from actor repository service") {
    val atomicHashMap = new AtomicReference[Map[UserLogin, UserCredentials]](Map.empty[UserLogin, UserCredentials])
    val repositoryCommandHandler = system.actorOf(Props(AsActor(new InMemoryKeyValueCommandHandler[Id, UserLogin, UserCredentials](atomicHashMap))))
    val repositoryQueryExecutor = system.actorOf(Props(AsActor[Id,Kvqe[UserLogin, UserCredentials]#I](new InMemoryKeyValueQueryExecutor[Id, UserLogin, UserCredentials](atomicHashMap))))
    val akkaCqrsRepository = new KeyValueCqrsRepository[Future, UserLogin, UserCredentials](
      CommandQueryController.actor[Id][KeyValueCommand[UserLogin, UserCredentials], Kvqe[UserLogin, UserCredentials]#I](repositoryCommandHandler, repositoryQueryExecutor)
    )

    val authCommandHandler = system.actorOf(Props(AsActor(new AuthenticationCommandHandler[FV, Future](akkaCqrsRepository, 10))))
    val authQueryExecutor = system.actorOf(Props(AsActor(new AuthenticationQueryExecutor[FV, Future](akkaCqrsRepository))))
    val akkaChainedCqrsAuthenticationService = new AuthenticationService[FV](CommandQueryController.actor[V](authCommandHandler, authQueryExecutor))

    whenReady(akkaChainedCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result1 =>
      result1 should equal (scalaz.Success(Acknowledged))
      whenReady(akkaChainedCqrsAuthenticationService.authenticate(UserLogin("test@test.com"), Password("password"))) {result2 =>
        result2 should equal (scalaz.Success(UserLogin("test@test.com")))
      }
      whenReady(akkaChainedCqrsAuthenticationService.createNewUser(UserLogin("test@test.com"), Password("password"))) { result3 =>
        result3 should equal (scalaz.Failure(scalaz.NonEmptyList("UserCredentialsAlreadyExistFailure(userLogin)")))
      }
    }
  }

}
