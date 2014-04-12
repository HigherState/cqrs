package org.higherState.cqrs.akka

import akka.actor._
import scala.reflect.ClassTag
import scala.concurrent.Future

import org.higherState.cqrs.{ValidationFailure, Service, FutureValid}
import scalaz._

trait AkkaValidationService extends Service[FutureValid] with ActorRefBuilder {

  import akka.pattern.ask
  implicit def timeout:akka.util.Timeout

  type R[+T] = Future[ValidationNel[ValidationFailure, T]]

  protected def commandHandler:ActorRef
  protected def query:ActorRef

  protected def dispatchCommand(c: => C): R[Unit] =
    commandHandler
      .ask(c)
      .mapTo[ValidationNel[ValidationFailure,Unit]]

  protected def executeQuery[T: ClassTag](qp: => QP):R[T] =
    query
      .ask(qp)
      .mapTo[ValidationNel[ValidationFailure,T]]
}

