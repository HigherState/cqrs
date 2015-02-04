package org.higherState.cqrs

import scala.reflect.ClassTag

trait Service[Out[+_]]

trait Dispatcher[Out[+_], M <: Message] {
  def sendMessage[T:ClassTag](m: => M):Out[T]
}

trait CQDispatcher[Out[+_], C <: Command, QP <: QueryParameters] {

  def sendCommand(c: => C):Out[Unit]

  def executeQuery[T:ClassTag](qp: => QP):Out[T]
}

object Dispatcher {
  def apply[Out[+_], M <: Message](messageReceiver:MessageReceiver[Out, M]) =
    new Dispatcher[Out, M] {
      def sendMessage[T: ClassTag](m: => M): Out[T] =
        messageReceiver.handle(m).asInstanceOf[Out[T]]
    }
}

object CQDispatcher {

  def apply[Out[+_], C <: Command, QP <: QueryParameters](commandHandler:CommandHandler[Out, C], queryExecutor:QueryExecutor[Out, QP]) =
    new CQDispatcher[Out, C, QP] {
      def sendCommand(c: => C): Out[Unit] =
        commandHandler.handle(c)
      def executeQuery[T: ClassTag](qp: => QP): Out[T] =
        queryExecutor.execute(qp).asInstanceOf[Out[T]]
    }
}


