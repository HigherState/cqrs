package org.higherState.cqrs.directives

import scalaz._
import scala.concurrent.{ExecutionContext, Future}

import org.higherState.cqrs._
import org.higherState.cqrs.ValidationException

trait Directives {

  type R[+T]

  def complete:R[Unit] =
    result[Unit](Unit)

  def result[T](value:T):R[T]
}

trait Bridge {
  this:Directives =>

  type R2[+T]

  def complete[T](value: => R2[T]):R[Unit] =
    onSuccess[T, Unit](value)(s => result[Unit](Unit))

  def success[T](value: => R2[T]):R[T] =
    onSuccess[T,T](value)(t => result(t))

  def onSuccess[S, T](value:R2[S])(f : (S) => R[T]): R[T]

  def foreach(func: => TraversableOnce[R2[Unit]]):R[Unit]

}

trait IdentityDirectives extends Directives {

  type R[+T] = T

  def result[T](value:T):R[T] =
    value

}

trait IdentityBridge extends Bridge {
  this:Directives =>

  type R2[+T] = T

  def onSuccess[S, T](value:R2[S])(f : (S) => R[T]) =
    f(value)

  def foreach(func: => TraversableOnce[R2[Unit]]):R[Unit] =
    complete
}

trait FutureDirectives extends Directives {

  implicit def executionContext:ExecutionContext

  type R[+T] = Future[T]

  def result[T](value:T):R[T] =
    Future.successful(value)
}

trait FutureBridge extends Bridge {
  this:FutureDirectives =>

  implicit def executionContext:ExecutionContext

  type R2[+T] = Future[T]

  def onSuccess[S, T](value:R2[S])(f : (S) => R[T]): R[T] =
    value.flatMap(f)

  def foreach(func: => TraversableOnce[R2[Unit]]):R[Unit] =
    Future.sequence(func).map(t => Unit)

}


trait ValidationDirectives extends Directives {

  def failure[T](failure: => ValidationFailure):R[T]

  def failures[T](failed: => NonEmptyList[ValidationFailure]):R[T]
}

trait ValidationOnlyDirectives extends ValidationDirectives {


  def result[T](value: T): R[T] =
    Success(value)

  type R[+T] = ValidationNel[ValidationFailure, T]

  def failure[T](failure: => ValidationFailure): R[T] =
    Failure(NonEmptyList(failure))

  def failures[T](failures: => NonEmptyList[ValidationFailure]):R[T] =
    Failure(failures)

}

trait ValidationOnlyBridge extends Bridge {
  this:ValidationDirectives =>

  type R2[+T] = ValidationNel[ValidationFailure, T]

  def foreach(func: => TraversableOnce[R2[Unit]]):R[Unit] =
    func.toIterator.collect {
      case Failure(f) => f
    }.reduceOption(_.append(_))
      .map(f => failures[Unit](f))
      .getOrElse(complete)


  def onSuccess[S, T](value:R2[S])(f : (S) => R[T]): R[T] =
    value match {
      case Failure(vf) =>
        failures[T](vf)
      case Success(s) =>
        f(s)
    }
}

trait FutureValidationDirectives extends ValidationDirectives {

  implicit def executionContext:ExecutionContext

  type R[+T] = Future[ValidationNel[ValidationFailure, T]]

  def failure[T](failure: => ValidationFailure) =
    Future.successful(Failure(NonEmptyList(failure)))

  def failures[T](failures: => NonEmptyList[ValidationFailure]):R[T] =
    Future.successful(Failure(failures))
}

trait FutureValidationBridge extends Bridge {

  this:FutureValidationDirectives =>

  type R2[+T] = Future[ValidationNel[ValidationFailure, T]]

  def foreach(func:TraversableOnce[R2[Unit]]):R[Unit] =
    Future.sequence(func).map(_.foldLeft[ValidationNel[ValidationFailure, Unit]](Success(Unit)) {
      case (v, Success(_)) =>
        v
      case (Failure(v), Failure(vf)) =>
        Failure(v.append(vf))
      case (_,c) =>
        c
    })

  def result[T](value: T) =
    Future.successful(Success(value))

  def onSuccess[S, T](value:R[S])(f : (S) => R[T]): R[T] =
    value.flatMap{
      case Success(s) =>
        f(s)
      case Failure(vf) =>
        failures(vf)
    }
}
