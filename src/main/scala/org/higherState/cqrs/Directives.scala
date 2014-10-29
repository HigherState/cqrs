package org.higherState.cqrs

import scalaz.{Failure, Success, NonEmptyList}
import scala.concurrent.{ExecutionContext, Future}

trait Directives extends Output {

  type Pipe[+S <: Service] = org.higherState.cqrs.Pipe[Out, S]

  def complete:Out[Unit] =
    unit[Unit](Unit)

  def unit[T](value: => T):Out[T]

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

  def unit[T](value: => T):Out[T] =
    value

  def map[T,U](output:Out[T])(f:T => U):Out[U] =
    f(output)

  def flatMap[T,U](r1:Out[T])(f:T => Out[U]):Out[U] =
    f(r1)

  def sequence[T,U](r: => TraversableOnce[Out[T]])(f:Seq[T] => Out[U]):Out[U] =
    f(r.toSeq)
}

trait ValidationDirectives extends FailureDirectives with Output.Valid {
  import scalaz.syntax.traverse._
  import scalaz.std.list._

  def unit[T](value: => T): Out[T] =
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

  def sequence[T,U](r: => TraversableOnce[Out[T]])(f:Seq[T] => Out[U]):Out[U] = {
    r.toList.sequence[Valid, T].flatMap(s => f(s))
  }

  def onFailure[T](value:Out[T])(f:NonEmptyList[ValidationFailure] => Out[T]):Out[T] =
    value match {
      case scalaz.Failure(vf) => f(vf)
      case v => v
    }

  def validationSequence[T,U](v:Iter[Out[T]])(f:Iter[Valid[T]] => Out[U]):Out[U] =
    f(v)
}

abstract class FutureDirectives(implicit val executionContext:ExecutionContext) extends Directives with Output.Future {

  def unit[T](value: => T):Out[T] =
    Future.successful(value)

  def map[T,U](output:Out[T])(f:T => U):Out[U] =
    output.map(f)

  def flatMap[T,U](output:Out[T])(f:T => Out[U]):Out[U] =
    output.flatMap(f)

  def sequence[T,U](r: => TraversableOnce[Out[T]])(f:Seq[T] => Out[U]):Out[U] =
    Future.sequence(r.toSeq).flatMap(i => f(i))
}

abstract class FutureValidationDirectives(implicit val executionContext:ExecutionContext) extends FailureDirectives with Output.FutureValid {
  import scalaz.syntax.traverse._
  import scalaz.std.list._

  def unit[T](value: => T):Out[T] =
    Future.successful(scalaz.Success(value))

  def map[T,U](output:Out[T])(f:T => U):Out[U] =
    output.map(_.map(f))

  def flatMap[T,U](output:Out[T])(f:T => Out[U]):Out[U] =
    output.flatMap{
      case Success(s) =>
        f(s)
      case Failure(vf) =>
        Future.successful(scalaz.Failure(vf))
    }

  def failure[T](failure: => ValidationFailure) =
    Future.successful(scalaz.Failure(NonEmptyList(failure)))

  def failures[T](failures: => NonEmptyList[ValidationFailure]):Out[T] =
    Future.successful(scalaz.Failure(failures))

  def failures[T](head: ValidationFailure, tail: Seq[ValidationFailure]): Out[T] =
    Future.successful(scalaz.Failure(NonEmptyList(head, tail:_*)))

  def sequence[T,U](r: => TraversableOnce[Out[T]])(f:Seq[T] => Out[U]):Out[U] =
    Future.sequence(r).flatMap { v =>
      v.toList.sequence[Valid, T] match {
        case scalaz.Success(s) =>
          f(s.toSeq)
        case scalaz.Failure(vf) =>
          Future.successful(scalaz.Failure(vf))
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
