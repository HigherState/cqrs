package org.higherState.cqrs

import scala.reflect.ClassTag
import scala.concurrent.{Future, ExecutionContext}

trait Service extends Output

trait Cqrs extends Service {

  type C <: Command
  type QP <: QueryParameters

  protected def dispatchCommand(c: => C):Out[Unit]

  protected def executeQuery[T:ClassTag](qp: => QP):Out[T]

}

trait CqrsService[_C <: Command, _QP <: QueryParameters] extends Cqrs {

  type C = _C
  type QP = _QP

}

trait IdentityCqrs extends Cqrs with Output.Identity {

  def commandHandler:CommandHandler[C] with Output.Identity

  def queryExecutor:QueryExecutor[QP] with Output.Identity

  protected def dispatchCommand(c: => C) {
    commandHandler.handle(c)
  }

  protected def executeQuery[T: ClassTag](qp: => QP): T = {
    queryExecutor.execute(qp).asInstanceOf[T]
  }
}

trait ValidationCqrs extends Cqrs with Output.Valid {

  def commandHandler:CommandHandler[C] with Output.Valid

  def queryExecutor:QueryExecutor[QP] with Output.Valid

  protected def dispatchCommand(c: => C) =
    commandHandler.handle(c)

  protected def executeQuery[T: ClassTag](qp: => QP): Valid[T] = {
    queryExecutor.execute(qp).asInstanceOf[Valid[T]]
  }
}

trait FutureCqrs extends Cqrs with Output.Future {

  def executionContext:ExecutionContext

  def commandHandler:CommandHandler[C] with Output.Future

  def queryExecutor:QueryExecutor[QP] with Output.Future

  protected def dispatchCommand(c: => C) =
    commandHandler.handle(c)

  protected def executeQuery[T: ClassTag](qp: => QP): Future[T] =
    queryExecutor.execute(qp).asInstanceOf[Future[T]]
}

trait FutureValidationCqrs extends Cqrs with Output.FutureValid {

  def executionContext:ExecutionContext

  def commandHandler:CommandHandler[C] with Output.FutureValid

  def queryExecutor:QueryExecutor[QP] with Output.FutureValid

  protected def dispatchCommand(c: => C) =
    commandHandler.handle(c)

  protected def executeQuery[T: ClassTag](qp: => QP): FutureValid[T] =
    queryExecutor.execute(qp).asInstanceOf[FutureValid[T]]
}







