package org.higherState.cqrs

import scala.concurrent.{TimeoutException, CanAwait, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Try
import scala.reflect.ClassTag

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

//    implicit def FutureFutureValidationPipe(implicit executionContext:ExecutionContext) = new SuccessPipe[Output.Future#Out, Output.FutureValid#Out] {
//      def success[T](value: => Output.Future#Out[T]): Output.FutureValid#Out[T] =
//        value.map(scalaz.Success(_))
//    }

  //implicit def with execution was not resolving so created future wrapper
  implicit val FutureFutureValidationPipe = new SuccessPipe[Output.Future#Out, Output.FutureValid#Out] {
    def success[T](value: => Output.Future#Out[T]): Output.FutureValid#Out[T] =
      value.map(scalaz.Success(_))(scala.concurrent.ExecutionContext.Implicits.global)
      //DelayedFuture(value)(scalaz.Success(_))
  }
}

trait FutureValidationPipes {

  implicit val FutureValidationFutureValidationPipe = new SuccessPipe[Output.FutureValid#Out, Output.FutureValid#Out] {
    def success[T](value: => Output.FutureValid#Out[T]): Output.FutureValid#Out[T] =
      value
  }
}

object Pipes extends IdentityPipes with ValidationPipes with FuturePipes with FutureValidationPipes


//Experimental wrapper as we don't have an execution context in implicit pipe
case class DelayedFuture[+T,T2](future:Future[T])(m:(T) => T2) extends Future[T2] {
  def value: Option[Try[T2]] = future.value.map(_.map(m))

  @throws(classOf[TimeoutException])
  @throws(classOf[InterruptedException])
  def ready(atMost: Duration)(implicit permit: CanAwait): this.type = {
    future.ready(atMost)
    this
  }

  @scala.throws[Exception](classOf[Exception])
  def result(atMost: Duration)(implicit permit: CanAwait): T2 =
    m(future.result(atMost))

  def onComplete[U](f: (Try[T2]) => U)(implicit executor: ExecutionContext) {
    future.onComplete(t => f(t.map(m)))
  }

  def isCompleted: Boolean =
    future.isCompleted
//
//  override def collect[S](pf: PartialFunction[T2, S])(implicit executor: ExecutionContext): Future[S] =
//    future.map(m).collect(pf)
//
//  override def flatMap[S](f: (T2) => Future[S])(implicit executor: ExecutionContext): Future[S] =
//    future.map(m).flatMap(f)
//
//  override def map[S](f: (T2) => S)(implicit executor: ExecutionContext): Future[S] =
//    future.map(m andThen f)
}