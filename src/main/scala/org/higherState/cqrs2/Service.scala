package org.higherState.cqrs2

import org.higherState.cqrs.{QueryParameters, Command}
import scala.reflect.ClassTag


trait Service[Out[+_]]

trait CqrsService[Out[+_], C <: Command, QP <: QueryParameters] extends Service[Out] {

  def dispatcher:Dispatcher[Out, C, QP]

}


trait Dispatcher[Out[+_], C <: Command, QP <: QueryParameters] {

  def sendCommand(c: => C):Out[Unit]

  def executeQuery[T:ClassTag](qp: => QP):Out[T]
}


