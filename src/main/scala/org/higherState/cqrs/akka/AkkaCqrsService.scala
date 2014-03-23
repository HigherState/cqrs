package org.higherState.cqrs.akka

import akka.actor._
import scala.reflect.ClassTag
import scala.concurrent.Future

import org.higherState.cqrs.{Query, CommandHandler, Service}

trait AkkaCqrsService extends Service with ActorRefBuilder {

  import akka.pattern.ask
  implicit def timeout:akka.util.Timeout

  type R[+T] = Future[T]

  protected def commandHandler:ActorRef
  protected def query:ActorRef

  protected def dispatchCommand(c: => C): R[Unit] =
    commandHandler
      .ask(c)
      .mapTo[Unit]

  protected def executeQuery[T: ClassTag](qp: => QP):R[T] =
    query
      .ask(qp)
      .mapTo[T]

}

