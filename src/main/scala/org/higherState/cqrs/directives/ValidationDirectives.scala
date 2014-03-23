package org.higherState.cqrs.directives

import scalaz._
import scala.concurrent.{ExecutionContext, Future}

import org.higherState.cqrs._
import org.higherState.cqrs.ValidationException

trait Directives {

  type R[+T]

  def complete:R[Unit] =
    result[Unit](Unit)

  def complete[T](value: => R[T]):R[Unit] =
    onSuccess[T, Unit](value)(s => result[Unit](Unit))

  def foreachComplete(func:TraversableOnce[R[Unit]]):R[Unit]

  def result[T](value:T):R[T]

  def onSuccess[S, T](value:R[S])(f : (S) => R[T]): R[T]

  def conditional[S, T](value:R[S], v:S => Option[R[T]])(f : (S) => R[T]):R[T]
}

trait IdentityDirectives extends Directives {

  type R[+T] = T

  def foreachComplete(func:TraversableOnce[R[Unit]]):Unit =
    Unit

  def result[T](value:T):R[T] =
    value

  def onSuccess[S, T](value:R[S])(f : (S) => R[T]) =
    f(value)

  def conditional[S, T](value:R[S], v:S => Option[R[T]])(f : (S) => R[T]) =
    v(value).getOrElse(f(value))
}

trait FutureDirectives extends Directives {

  implicit def executionContext:ExecutionContext

  type R[+T] = Future[T]

  def foreachComplete(func:TraversableOnce[R[Unit]]):R[Unit] =
    Future.sequence(func).map(t => Unit)

  def result[T](value:T):R[T] =
    Future.successful(value)

  def onSuccess[S, T](value:R[S])(f : (S) => R[T]): R[T] =
    value.flatMap(f)

  def conditional[S, T](value:R[S], v:S => Option[R[T]])(f : (S) => R[T]):R[T] =
    onSuccess(value){ s =>
      v(s).getOrElse(f(s))
    }
}


trait ValidationDirectives extends Directives {

  def failed[T](failure: => ValidationFailure):R[T]
}

trait ValidationOnlyDirectives extends ValidationDirectives {

  type R[+T] = ValidationNel[ValidationFailure, T]

  def foreachComplete(func:TraversableOnce[R[Unit]]):R[Unit] =
    func.foldLeft(complete){
      case (v, Success(_)) =>
        v
      case (Failure(v), Failure(vf)) =>
        Failure(v.append(vf))
      case (_,c) =>
        c
    }

  def failed[T](failure: => ValidationFailure): R[T] =
    Failure(NonEmptyList(failure))

  def result[T](value: T): R[T] =
    Success(value)

  def onSuccess[S, T](value:R[S])(f : (S) => R[T]): R[T] =
    value.flatMap(f)

  def conditional[S, T](value:R[S], v:S => Option[R[T]])(f : (S) => R[T]):R[T] =
    value.flatMap{s =>
      v(s).getOrElse(f(s))
    }
}

trait FutureValidationDirectives extends ValidationDirectives {

  implicit def executionContext:ExecutionContext

  type R[+T] = Future[ValidationNel[ValidationFailure, T]]

  def foreachComplete(func:TraversableOnce[R[Unit]]):R[Unit] =
    Future.sequence(func).map(_.foldLeft[ValidationNel[ValidationFailure, Unit]](Success(Unit)) {
      case (v, Success(_)) =>
        v
      case (Failure(v), Failure(vf)) =>
        Failure(v.append(vf))
      case (_,c) =>
        c
    })

  def failed[T](failure: => ValidationFailure) =
    Future.failed(ValidationException(failure))

  def result[T](value: T) =
    Future.successful(Success(value))

  def onSuccess[S, T](value:R[S])(f : (S) => R[T]): R[T] =
    value.flatMap{
      case Success(s) =>
        f(s)
      case Failure(vf) =>
        Future.successful(Failure(vf))
    }

  def conditional[S, T](value:R[S], v:S => Option[R[T]])(f : (S) => R[T]):R[T] =
    onSuccess(value){ s =>
      v(s).getOrElse(f(s))
    }
}
