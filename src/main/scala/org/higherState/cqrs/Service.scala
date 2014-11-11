package org.higherState.cqrs

import scala.reflect.ClassTag

trait Service[Out[+_]]

trait Dispatcher[Out[+_], C <: Command, QP <: QueryParameters] {

  def sendCommand(c: => C):Out[Unit]

  def executeQuery[T:ClassTag](qp: => QP):Out[T]
}

object Dispatcher {

  def apply[Out[+_], C <: Command, QP <: QueryParameters](commandHandler:CommandHandler[Out, C], queryExecutor:QueryExecutor[Out, QP]) =
    new Dispatcher[Out, C, QP] {
      def sendCommand(c: => C): Out[Unit] =
        commandHandler.handle(c)
      def executeQuery[T: ClassTag](qp: => QP): Out[T] =
        queryExecutor.execute(qp).asInstanceOf[Out[T]]
    }
}


