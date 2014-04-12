package org.higherState.cqrs.akka

import akka.actor._
import scala.reflect.ClassTag
import scala.concurrent.Future

import org.higherState.cqrs.{Query, CommandHandler, Service}

trait AkkaCqrsService extends Service[Future] with ActorRefBuilder {

  import akka.pattern.ask
  implicit def timeout:akka.util.Timeout

  protected def commandHandler:ActorRef
  protected def query:ActorRef

  protected def dispatchCommand(c: => C): Future[Unit] =
    commandHandler
      .ask(c)
      .mapTo[Unit]

  protected def executeQuery[T: ClassTag](qp: => QP):Future[T] =
    query
      .ask(qp)
      .mapTo[T]

}

