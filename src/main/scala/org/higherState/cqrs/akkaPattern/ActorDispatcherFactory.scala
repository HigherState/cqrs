package org.higherState.cqrs.akkaPattern

import org.higherState.cqrs._
import scala.concurrent.Future
import scala.reflect.ClassTag
import akka.actor.ActorRef
import akka.util.Timeout

object ActorDispatcherFactory {

  import akka.pattern.ask

  def future[C <: Command, QP <: QueryParameters](commandHandler:ActorRef, queryExecutor:ActorRef)(implicit timeout:Timeout) =
    new Dispatcher[Future, C, QP] {

      def sendCommand(c: => C): Future[Unit] =
        commandHandler
          .ask(c)
          .mapTo[Unit]

      def executeQuery[T: ClassTag](qp: => QP): Future[T] =
        queryExecutor
          .ask(qp)
          .mapTo[T]
    }

  def futureValid[C <: Command, QP <: QueryParameters](commandHandler:ActorRef, queryExecutor:ActorRef)(implicit timeout:Timeout) =
    new Dispatcher[FutureValid, C, QP] {
      def sendCommand(c: => C): FutureValid[Unit] =
        commandHandler
          .ask(c)
          .mapTo[Valid[Unit]]

      def executeQuery[T: ClassTag](qp: => QP): FutureValid[T] =
        queryExecutor
          .ask(qp)
          .mapTo[Valid[T]]
    }
}
