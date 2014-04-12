package org.higherState.cqrs

import scala.reflect.ClassTag

trait Service[R[_]]

trait CqrsService[R[_]] extends Service[R] {

  type C <: Command
  type QP <: QueryParameters

  protected def dispatchCommand(c: => C):R[Unit]

  protected def executeQuery[T:ClassTag](qp: => QP):R[T]
}