package org.higherState.cqrs

import scala.reflect.ClassTag

trait Service extends Output

trait PipedService[S <: Service] extends Pipe {
  def service:S{type Out[+T] = In[T]}

  def apply[T](f:this.type => Out[T]) =
    f(this)
}

trait WiredPipes extends Directives {
  w =>

  trait WiredService[S <: Service] extends Pipe {
    def service:S{type Out[+T] = In[T]}
    type Out[+T] <: w.Out[T]

    def apply[T](f:this.type => Out[T]):Out[T] =
      f(this)
  }
}


trait Cqrs {

  type C <: Command
  type QP <: QueryParameters
}

trait CqrsService[_C <: Command, _QP <: QueryParameters] extends Service with Cqrs {

  type C = _C
  type QP = _QP
  protected def dispatchCommand(c: => C):Out[Unit]

  protected def executeQuery[T:ClassTag](qp: => QP):Out[T]
}

trait IdentityCqrs extends Cqrs with Output.Identity {

  def commandHandler:CommandHandler[C] with Output.Identity

  def query:Query[QP] with Output.Identity

  protected def dispatchCommand(c: => C): Unit = {
    commandHandler.handle(c)
  }

  protected def executeQuery[T: ClassTag](qp: => QP): T = {
    query.execute(qp).asInstanceOf[T]
  }
}





