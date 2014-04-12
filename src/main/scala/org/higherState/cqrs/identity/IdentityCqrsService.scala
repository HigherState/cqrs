package org.higherState.cqrs.identity

import org.higherState.cqrs._
import scala.reflect.ClassTag

trait IdentityCqrsService extends CqrsService[Identity] {
  s =>

  def commandHandler:CommandHandler{type C = s.C; type Out[T] = T}

  def query:Query{type QP = s.QP; type Out[T] = T}

  protected def dispatchCommand(c: => C): Unit = {
    commandHandler.handle(c)
  }

  protected def executeQuery[T: ClassTag](qp: => QP): T = {
    query.execute(qp).asInstanceOf[T]
  }
}
