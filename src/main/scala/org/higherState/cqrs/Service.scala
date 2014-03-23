package org.higherState.cqrs

import scala.reflect.ClassTag
import scala.concurrent.Future

trait Service {
  type R[+T]
}

trait CqrsService extends Service {

  type C <: Command
  type QP <: QueryParameters

  protected def dispatchCommand(c: => C):R[Unit]

  protected def executeQuery[T:ClassTag](qp: => QP):R[T]
}

trait Repository extends Service {
  type R[+T] = T
}

