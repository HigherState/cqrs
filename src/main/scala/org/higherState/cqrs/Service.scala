package org.higherState.cqrs

import scala.reflect.ClassTag

trait Service[Out[+_]]

trait CqrsService[Out[+_]] extends Service[Out] {

  def dispatcher:Dispatcher[Out]
}

trait Dispatcher[Out[+_]] {

  def sendCommand(c: => Command):Out[Unit]

  def executeQuery[T:ClassTag](qp: => QueryParameters):Out[T]
}

object Dispatcher {

  def apply[Out[+_]](commandHandler:CommandHandler[Out, Command], queryExecutor:QueryExecutor[Out, QueryParameters]) =
    new Dispatcher[Out] {
      def sendCommand(c: => Command): Out[Unit] =
        commandHandler.handle(c)
      def executeQuery[T: ClassTag](qp: => QueryParameters): Out[T] =
        queryExecutor.execute(qp).asInstanceOf[Out[T]]
    }
}


