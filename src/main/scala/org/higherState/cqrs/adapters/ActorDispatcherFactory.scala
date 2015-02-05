package org.higherState.cqrs.adapters

import org.higherState.cqrs._
import scala.concurrent.Future
import scala.reflect.ClassTag
import akka.actor.ActorRef
import akka.util.Timeout
import org.higherState.cqrs.std.{Valid, FutureValid}

object ActorDispatcherFactory {

  import akka.pattern.ask

  def future[C <: Command, QP <: QueryParameters](commandHandler:ActorRef, queryExecutor:ActorRef)(implicit timeout:Timeout) =
    new CQDispatcher[Future, C, QP] {

      def sendCommand(c: => C): Future[Unit] =
        commandHandler
          .ask(c)
          .mapTo[Unit]

      def executeQuery[T: ClassTag](qp: => QP): Future[T] =
        queryExecutor
          .ask(qp)
          .mapTo[T]
    }

  def futureValid[C <: Command, QP <: QueryParameters, E](commandHandler:ActorRef, queryExecutor:ActorRef)(implicit timeout:Timeout) =
    new CQDispatcher[({type V[+T] = FutureValid[E,T]})#V, C, QP] {
      def sendCommand(c: => C): FutureValid[E, Unit] =
        commandHandler
          .ask(c)
          .mapTo[Valid[E, Unit]]

      def executeQuery[T: ClassTag](qp: => QP): FutureValid[E, T] =
        queryExecutor
          .ask(qp)
          .mapTo[Valid[E, T]]
    }
}
