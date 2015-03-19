package org.higherState.cqrs

import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration

trait Service[Out[+_]]

trait Dispatcher[Out[+_], M <: Message] {
  def sendMessage[T:ClassTag](m: => M):Out[T]
}

trait CQDispatcher[Out[+_], C <: Command, QP <: QueryParameters] {

  def sendCommand(c: => C):Out[Unit]

  def sendCommand(c: => C, delay:FiniteDuration): Out[Unit]

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
      def sendCommand(c: => C, delay:FiniteDuration): Out[Unit] =
        commandHandler.handle(c)
      def executeQuery[T: ClassTag](qp: => QP): Out[T] =
        queryExecutor.execute(qp).asInstanceOf[Out[T]]
    }

  def apply[Out[+_], C <: Command](commandHandler:CommandHandler[Out, C]) =
    new CQDispatcher[Out, C, Nothing] {
      def sendCommand(c: => C): Out[Unit] =
        commandHandler.handle(c)
      def sendCommand(c: => C, delay:FiniteDuration): Out[Unit] =
        commandHandler.handle(c)
      def executeQuery[T: ClassTag](qp: => Nothing): Out[T] = ???
    }

  def apply[Out[+_], QP <: QueryParameters](queryExecutor:QueryExecutor[Out, QP]) =
    new CQDispatcher[Out, Nothing, QP] {

      def sendCommand(c: => Nothing): Out[Unit] = ???
      def sendCommand(c: => Nothing, delay:FiniteDuration): Out[Unit] = ???
      def executeQuery[T: ClassTag](qp: => QP): Out[T] =
        queryExecutor.execute(qp).asInstanceOf[Out[T]]
    }
}


