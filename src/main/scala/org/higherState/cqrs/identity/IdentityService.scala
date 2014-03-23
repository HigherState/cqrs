package org.higherState.cqrs.identity

import org.higherState.cqrs._
import scala.reflect.ClassTag

trait IdentityCqrsService extends CqrsService {
  s =>

  type R[+T] = T

  def commandHandler:CommandHandler{type C = s.C; type R[T] = s.R[T]}

  def query:Query{type QP = s.QP; type R[T] = s.R[T]}

  protected def dispatchCommand(c: => C): R[Unit] = {
    commandHandler.handle(c)
  }

  protected def executeQuery[T: ClassTag](qp: => QP): R[T] = {
    query.execute(qp).asInstanceOf[R[T]]
  }
}
