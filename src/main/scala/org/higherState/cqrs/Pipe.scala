package org.higherState.cqrs

import scala.concurrent.{ExecutionContext, Future}

case class Pipe[Out[_], +S <: Service](from:S)(implicit ev:SuccessPipe[S#Out, Out]) {

  def apply[In[_], T](f: S => In[T]):Out[T] =
    //forced casting, havent found a better way to do this without causing variance issues
    ev.success(f(from).asInstanceOf[S#Out[T]])
}

trait SuccessPipe[In[_], Out[_]] {
  def success[T](value: => In[T]):Out[T]
}

trait IdentityPipes {

  implicit val IdentityIdentityPipe = new SuccessPipe[Output.Identity#Out, Output.Identity#Out] {
    def success[T](value: => Output.Identity#Out[T]): Output.Identity#Out[T] =
      value
  }

  implicit val IdentityValidationPipe = new SuccessPipe[Output.Identity#Out, Output.Valid#Out] {
    def success[T](value: => Output.Identity#Out[T]): Output.Valid#Out[T] =
      scalaz.Success(value)
  }

  implicit val IdentityFuturePipe = new SuccessPipe[Output.Identity#Out, Output.Future#Out] {
    def success[T](value: => Output.Identity#Out[T]): Output.Future#Out[T] =
      Future.successful(value)
  }

  implicit val IdentityFutureValidationPipe = new SuccessPipe[Output.Identity#Out, Output.FutureValid#Out] {
    def success[T](value: => Output.Identity#Out[T]): Output.FutureValid#Out[T] =
      Future.successful(scalaz.Success(value))
  }
}

trait ValidationPipes {

  implicit val ValidationValidationPipe = new SuccessPipe[Output.Valid#Out, Output.Valid#Out] {
    def success[T](value: => Output.Valid#Out[T]): Output.Valid#Out[T] =
      value
  }

  implicit val ValidationFutureValidationPipe = new SuccessPipe[Output.Valid#Out, Output.FutureValid#Out] {
    def success[T](value: => Output.Valid#Out[T]): Output.FutureValid#Out[T] =
      Future.successful(value)
  }
}

trait FuturePipes {

  implicit val FutureFuturePipe = new SuccessPipe[Output.Future#Out, Output.Future#Out] {
    def success[T](value: => Output.Future#Out[T]): Output.Future#Out[T] =
      value
  }

  implicit def FutureFutureValidationPipe(implicit exectionContext:ExecutionContext) = new SuccessPipe[Output.Future#Out, Output.FutureValid#Out] {
    def success[T](value: => Output.Future#Out[T]): Output.FutureValid#Out[T] =
      value.map(scalaz.Success(_))
  }
}

trait FutureValidationPipes {

  implicit val FutureValidationFutureValidationPipe = new SuccessPipe[Output.FutureValid#Out, Output.FutureValid#Out] {
    def success[T](value: => Output.FutureValid#Out[T]): Output.FutureValid#Out[T] =
      value
  }
}

object Pipes extends IdentityPipes with ValidationPipes with FuturePipes with FutureValidationPipes