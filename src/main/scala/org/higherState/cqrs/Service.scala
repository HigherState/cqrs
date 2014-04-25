package org.higherState.cqrs

import scala.reflect.ClassTag

trait Service[R[_]]

trait PipedService[S[R[+_]] <: Service[R]] extends Pipe {
  def service:S[In]

  def apply[T](f:this.type => Out[T]) =
    f(this)
}

trait WiredPipes extends Directives {
  w =>

  trait WiredService[S[R[+_]] <: Service[R]] extends Pipe {
    def service:S[In]
    type Out[+T] <: w.Out[T]

    def apply[T](f:this.type => Out[T]):Out[T] =
      f(this)
  }
}


trait Cqrs {

  type C <: Command
  type QP <: QueryParameters
}

trait CqrsService[R[_], _C <: Command, _QP <: QueryParameters] extends Service[R] with Cqrs {

  type C = _C
  type QP = _QP
  protected def dispatchCommand(c: => C):R[Unit]

  protected def executeQuery[T:ClassTag](qp: => QP):R[T]
}

trait IdentityCqrs extends Cqrs {
  s =>

  def commandHandler:CommandHandler[C] with Output.Identity

  def query:Query[QP] with Output.Identity

  protected def dispatchCommand(c: => C): Unit = {
    commandHandler.handle(c)
  }

  protected def executeQuery[T: ClassTag](qp: => QP): T = {
    query.execute(qp).asInstanceOf[T]
  }
}





