package org.higherState.cqrs

import scalaz.{Failure, Success, NonEmptyList}
import scala.concurrent.{ExecutionContext, Future}

trait Directives extends Output {

  def complete:Out[Unit] =
    bind[Unit](Unit)

  def bind[T](value: => T):Out[T]

  def flatMap[T,U](output:Out[T])(f:T => Out[U]):Out[U]

  def map[T,U](output:Out[T])(f:T => U):Out[U]

  def sequence[T,U](r1:Out[T], r2:Out[T], r:Out[T]*)(f:Seq[T] => Out[U]):Out[U] =
    sequence[T,U](r1 :: r2 :: r.toList)(f)

  def sequence[T,U](r: => TraversableOnce[Out[T]])(f:Seq[T] => Out[U]):Out[U]

  def foreach(r1:Out[Unit], r2:Out[Unit], r:Out[Unit]*):Out[Unit] =
    sequence[Unit, Unit](r1 :: r2 :: r.toList)(_ => complete)

  def foreach(f: => TraversableOnce[Out[Unit]]):Out[Unit] =
    sequence[Unit, Unit](f)(_ => complete)
}

trait FailureDirectives extends Directives {

  def failure[T](failure: => ValidationFailure):Out[T]

  def failures[T](failed: => NonEmptyList[ValidationFailure]):Out[T]

  def failures[T](head:ValidationFailure, tail:Seq[ValidationFailure]):Out[T]

  def onValid[T, U](v:Valid[T])(f:T => Out[U]):Out[U] =
    v match {
      case Success(t) =>
        f(t)
      case Failure(vf) =>
        failures[U](vf)
    }

  def onFailure[T](value:Out[T])(f:NonEmptyList[ValidationFailure] => Out[T]):Out[T]

  def validationSequence[T,U](v:Iter[Out[T]])(f:Iter[Valid[T]] => Out[U]):Out[U]
}

/*Output fixed*/
trait IdentityDirectives extends Directives with Output.Identity {

  def bind[T](value: => T):Out[T] =
    value

  def map[T,U](output:Out[T])(f:T => U):Out[U] =
    f(output)

  def flatMap[T,U](r1:Out[T])(f:T => Out[U]):Out[U] =
    f(r1)

  def sequence[T,U](r: => TraversableOnce[Out[T]])(f:Seq[T] => Out[U]):Out[U] =
    f(r.toSeq)
}

trait ValidationDirectives extends FailureDirectives with Output.Valid {

  def bind[T](value: => T): Out[T] =
    Success(value)

  def failure[T](failure: => ValidationFailure): Out[T] =
    Failure(NonEmptyList(failure))

  def failures[T](failures: => NonEmptyList[ValidationFailure]):Out[T] =
    Failure(failures)

  def failures[T](head:ValidationFailure, tail:Seq[ValidationFailure]):Out[T] =
    Failure(NonEmptyList(head, tail:_*))

  def map[T,U](output:Out[T])(f:T => U):Out[U] =
    output.map(f)

  def flatMap[T,U](r1:Out[T])(f:T => Out[U]):Out[U] =
    r1.flatMap(f)

  def sequence[T,U](r: => TraversableOnce[Out[T]])(f:Seq[T] => Out[U]):Out[U] =
    r.sequence.flatMap(f)

  def onFailure[T](value:Out[T])(f:NonEmptyList[ValidationFailure] => Out[T]):Out[T] =
    value match {
      case scalaz.Failure(vf) => f(vf)
      case v => v
    }

  def validationSequence[T,U](v:Iter[Out[T]])(f:Iter[Valid[T]] => Out[U]):Out[U] =
    f(v)
}

trait FutureDirectives extends Directives with Output.Future {

  implicit def executionContext:ExecutionContext

  def bind[T](value: => T):Out[T] =
    Future.successful(value)

  def map[T,U](output:Out[T])(f:T => U):Out[U] =
    output.map(f)

  def flatMap[T,U](output:Out[T])(f:T => Out[U]):Out[U] =
    output.flatMap(f)

  def sequence[T,U](r: => TraversableOnce[Out[T]])(f:Seq[T] => Out[U]):Out[U] =
    Future.sequence(r.toSeq).flatMap(i => f(i))
}

trait FutureValidationDirectives extends FailureDirectives with Output.FutureValid {

  implicit def executionContext:ExecutionContext

  def bind[T](value: => T):Out[T] =
    Future.successful(Success(value))

  def map[T,U](output:Out[T])(f:T => U):Out[U] =
    output.map(_.map(f))

  def flatMap[T,U](output:Out[T])(f:T => Out[U]):Out[U] =
    output.flatMap{
      case Success(s) =>
        f(s)
      case Failure(vf) =>
        Future.successful(Failure(vf))
    }

  def failure[T](failure: => ValidationFailure) =
    Future.successful(Failure(NonEmptyList(failure)))

  def failures[T](failures: => NonEmptyList[ValidationFailure]):Out[T] =
    Future.successful(Failure(failures))

  def failures[T](head: ValidationFailure, tail: Seq[ValidationFailure]): Out[T] =
    Future.successful(Failure(NonEmptyList(head, tail:_*)))

  def sequence[T,U](r: => TraversableOnce[Out[T]])(f:Seq[T] => Out[U]):Out[U] =
    Future.sequence(r).flatMap { v =>
      v.sequence match {
        case Success(s) =>
          f(s)
        case Failure(vf) =>
          Future.successful(Failure(vf))
      }
    }

  def onFailure[T](value:Out[T])(f:NonEmptyList[ValidationFailure] => Out[T]):Out[T] =
    value flatMap {
      case scalaz.Failure(vf) => f(vf)
      case v => value
    }

  def validationSequence[T,U](v:Iter[Out[T]])(f:Iter[Valid[T]] => Out[U]):Out[U] =
    Future.sequence(v).flatMap{f}
}

sealed trait Pipe extends Directives with Input {
  protected def success[T](value: => In[T]):Out[T]
}

trait ServicePipesDirectives extends Directives {
  container =>
  import scala.language.higherKinds

  trait ServicePipe[S <: Service] extends Pipe {
    def service:S{type Out[+T] = In[T]}

    type Out[+T] <: container.Out[T]

    def apply[T, U](f: S{type Out[T] = In[T]} => In[T])(m:T => container.Out[U]):container.Out[U] =
      container.flatMap {
        success(f(service))
      }(m)

    def transform[T](f: S{type Out[T] = In[T]} => In[T]):container.Out[T] =
      container.map {
        success(f(service))
      }{t => t}
  }
}

trait IdentityPipe extends Pipe with Input.Identity {
  def success[T](value: => In[T]):Out[T] =
    bind(value)
}

trait ValidationPipe extends Pipe with FailureDirectives with Input.Valid {
  def success[T](value: => In[T]):Out[T] =
    value match {
      case Failure(vf) =>
        failures[T](vf)
      case Success(s) =>
        bind(s)
    }
}

trait FuturePipe extends Pipe with FutureDirectives with Input.Future {
  def success[T](value: => In[T]):Out[T] =
    value
}

trait FutureValidationPipe extends Pipe with FutureValidationDirectives with Input.FutureValid {
  def success[T](value: => In[T]):Out[T] =
    value
}