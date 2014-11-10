package org.higherState.cqrs

import scala.concurrent.{ExecutionContext, CanAwait, TimeoutException, Future}
import scala.util.{Success, Failure, Try}
import scala.concurrent.duration.Duration

case class FlattenFuture[+T,T2](future:Future[T])(m:(T) => Future[T2]) extends Future[T2] {
  def value: Option[Try[T2]] =
    future.value.flatMap {
      case Success(t) =>
        m(t).value
      case Failure(f) =>
        Some(scala.util.Failure(f))
    }

  @throws(classOf[TimeoutException])
  @throws(classOf[InterruptedException])
  def ready(atMost: Duration)(implicit permit: CanAwait): this.type = {
    future.ready(atMost)
    this
  }

  @scala.throws[Exception](classOf[Exception])
  def result(atMost: Duration)(implicit permit: CanAwait): T2 =
    m(future.result(atMost)).result(atMost)

  def onComplete[U](f: (Try[T2]) => U)(implicit executor: ExecutionContext) {
    future.onComplete{
      case Success(t) =>
        m(t).onComplete(t => f(t))
      case Failure(vf) =>
        f(Failure(vf))
    }
  }

  def isCompleted: Boolean =
    future.isCompleted
}

case class MapFuture[+T,T2](future:Future[T])(m:(T) => T2) extends Future[T2] {
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
}
