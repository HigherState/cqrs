package org.higherState.cqrs

import scalaz.{Failure, Success, NonEmptyList}
import scala.concurrent.{ExecutionContext, Future}

trait Directives extends Output {

  def complete:Out[Unit] =
    result[Unit](Unit)

  def result[T](value:T):Out[T]
  
  //use shapeless
  def merge[T,U,W](r1:Out[T], r2:Out[U])(f:(T,U) => Out[W]):Out[W]
}

trait IdentityDirectives extends Directives with Output.Identity {

  def result[T](value:T):Out[T] =
    value

  def merge[T, U, W](r1: Out[T], r2: Out[U])(f: (T, U) => Out[W]): Out[W] =
    f(r1,r2)
}

trait FailureDirectives extends Directives {

  def failure[T](failure: => ValidationFailure):Out[T]

  def failures[T](failed: => NonEmptyList[ValidationFailure]):Out[T]
}

trait ValidationDirectives extends FailureDirectives with Output.Valid {

  def result[T](value: T): Out[T] =
    Success(value)

  def failure[T](failure: => ValidationFailure): Out[T] =
    Failure(NonEmptyList(failure))

  def failures[T](failures: => NonEmptyList[ValidationFailure]):Out[T] =
    Failure(failures)

  def merge[T, U, W](r1: Out[T], r2: Out[U])(f: (T, U) => Out[W]): Out[W] =
    r1 -> r2 match {
      case (Success(s1), Success(s2)) =>
        f(s1, s2)
      case (Failure(f1), Failure(f2)) =>
        Failure(f1.append(f2))
      case (Failure(f1), _) =>
        Failure(f1)
      case (_, Failure(f2)) =>
        Failure(f2)
    }
}

trait FutureDirectives extends Directives with Output.Future {

  implicit def executionContext:ExecutionContext

  def result[T](value:T):Out[T] =
    Future.successful(value)

  def merge[T, U, W](r1: Out[T], r2: Out[U])(f: (T, U) => Out[W]): Out[W] =
    r1.flatMap { fs1 =>
        r2.flatMap { fs2 =>
          f(fs1, fs2)
        }
    }
}

trait FutureValidationDirectives extends FailureDirectives with Output.FutureValid {

  implicit def executionContext:ExecutionContext

  def result[T](value:T):Out[T] =
    Future.successful(Success(value))

  def merge[T, U, W](r1: Out[T], r2: Out[U])(f: (T, U) => Out[W]): Out[W] =
    r1.flatMap{fs1 =>
      r2.flatMap{fs2 =>
        fs1 -> fs2 match {
          case (Success(s1), Success(s2)) =>
            f(s1, s2)
          case (Failure(f1), Failure(f2)) =>
            Future.successful(Failure(f1.append(f2)))
          case (Failure(f1), _) =>
            Future.successful(Failure(f1))
          case (_, Failure(f2)) =>
            Future.successful(Failure(f2))
        }
      }
    }

  def failure[T](failure: => ValidationFailure) =
    Future.successful(Failure(NonEmptyList(failure)))

  def failures[T](failures: => NonEmptyList[ValidationFailure]):Out[T] =
    Future.successful(Failure(failures))
}