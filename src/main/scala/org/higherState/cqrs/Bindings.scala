package org.higherState.cqrs

import scalaz._

//Bindings, doesnt seem the best name
trait MonadBind[Out[+_]] extends Monad[Out]{

  implicit protected def monad:Monad[Out]

  def bind[A, B](fa: Out[A])(f: (A) => Out[B]): Out[B] =
    monad.bind(fa)(f)

  def point[A](a: => A): Out[A] =
    monad.point(a)
}

trait ValidatorBind[E,Out[+_]] extends Validator[E, Out] {

  implicit protected def validator:Validator[E, Out]

  def bind[A, B](fa: Out[A])(f: (A) => Out[B]): Out[B] =
    validator.bind(fa)(f)

  def point[A](a: => A): Out[A] =
    validator.point(a)

  def failure(validationFailure: => E): Out[Nothing] =
    validator.failure(validationFailure)

  def failures(validationFailures: => NonEmptyList[E]): Out[Nothing] =
    validator.failures(validationFailures)
}


