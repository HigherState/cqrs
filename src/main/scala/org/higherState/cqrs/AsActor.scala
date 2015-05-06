package org.higherState.cqrs

import akka.actor.Actor
import scala.concurrent.Future
import akka.pattern.PipeableFuture

sealed trait PipeableActor extends Actor {
  import context.dispatcher

  protected def pipeSender: PartialFunction[Any, Unit] = {
    case f:Future[_] =>
      new PipeableFuture(f) pipeTo sender
    case a =>
      sender ! a
  }
}

object AsActor {
  def apply[Out[+_], E <: Event](listener:ServiceListener[Out, E])(implicit fold:Fold[Out]):Actor =
    new Actor {
      def receive: Receive =
        listener.handle.andThen(t => fold.apply[Unit](t)(_ => Unit)).asInstanceOf[Receive]
    }

  def apply[Out[+_], C <: Command](commandHandler:CommandHandler[Out, C]):Actor =
    new PipeableActor {
      def receive = {
        case c:Command =>
          pipeSender(commandHandler.handle(c.asInstanceOf[C]))
      }
    }

  def apply[Out[+_], QP <: QueryParameters](queryExecutor:QueryExecutor[Out, QP]):Actor =
    new PipeableActor {
      def receive = {
        case qp:QueryParameters =>
          pipeSender(queryExecutor.execute(qp.asInstanceOf[QP]))
      }
    }

  def apply[Out[+_], M <: Message](messageReceiver:MessageReceiver[Out, M]):Actor =
    new PipeableActor {
      def receive = {
        case m:Message =>
          pipeSender(messageReceiver.handle(m.asInstanceOf[M]))
      }
    }

  def fold[Out[+_]] = new FoldParamCurry[Out]
}

final class FoldParamCurry[Out[+_]] {
  def apply[Out2[+_], C <: Command](commandHandler:CommandHandler[({type X[+T] = Out[Out2[T]]})#X, C])(implicit fold:Fold[Out]) =
    new PipeableActor {
      def receive = {
        case c:Command =>
          fold(commandHandler.handle(c.asInstanceOf[C]))(pipeSender)
      }
    }

  def apply[Out2[+_], QP <: QueryParameters](queryExecutor:QueryExecutor[({type X[+T] = Out[Out2[T]]})#X, QP])(implicit fold:Fold[Out]) =
    new PipeableActor {
      def receive = {
        case qp:QueryParameters =>
          fold(queryExecutor.execute(qp.asInstanceOf[QP]))(pipeSender)
      }
    }

  def apply[Out2[+_], M <: Message](messageReceiver:MessageReceiver[({type X[+T] = Out[Out2[T]]})#X, M])(implicit fold:Fold[Out]) =
    new PipeableActor {
      def receive = {
        case m:Message =>
          fold(messageReceiver.handle(m.asInstanceOf[M]))(pipeSender)
      }
    }
}
