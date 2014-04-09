package org.higherState.cqrs.directives

import scalaz._
import scala.concurrent.{ExecutionContext, Future}

import org.higherState.cqrs._
import org.higherState.cqrs.ValidationException

trait Directives {

  type Out[+T]

  def complete:Out[Unit] =
    result[Unit](Unit)

  def result[T](value:T):Out[T]
  
  //user shapeless
  def merge[T,U,W](r1:Out[T],r2:Out[U])(f:(Out[T],Out[U]) => Out[W]):Out[W] =
    f(r1,r2)
}

trait FailureDirectives extends Directives {

  def failure[T](failure: => ValidationFailure):Out[T]

  def failures[T](failed: => NonEmptyList[ValidationFailure]):Out[T]
}

trait ValidationDirectives extends FailureDirectives {

  def result[T](value: T): Out[T] =
    Success(value)

  type Out[+T] = ValidationNel[ValidationFailure, T]

  def failure[T](failure: => ValidationFailure): Out[T] =
    Failure(NonEmptyList(failure))

  def failures[T](failures: => NonEmptyList[ValidationFailure]):Out[T] =
    Failure(failures)

}

trait IdentityDirectives extends Directives {

  type Out[+T] = T

  def result[T](value:T):Out[T] =
    value

}

trait FutureDirectives extends Directives {

  implicit def executionContext:ExecutionContext

  type Out[+T] = Future[T]

  def result[T](value:T):Out[T] =
    Future.successful(value)
}

trait FutureValidationDirectives extends FailureDirectives {

  implicit def executionContext:ExecutionContext

  type Out[+T] = Future[ValidationNel[ValidationFailure, T]]

  def failure[T](failure: => ValidationFailure) =
    Future.successful(Failure(NonEmptyList(failure)))

  def failures[T](failures: => NonEmptyList[ValidationFailure]):Out[T] =
    Future.successful(Failure(failures))
}