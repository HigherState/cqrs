package org.higherState.cqrs

import scalaz.{NonEmptyList, Monad}

trait FMonad[E, Out[+_]] extends Monad[Out] {

  def failure(validationFailure: => E):Out[Nothing]

  def failures(validationFailures: => NonEmptyList[E]):Out[Nothing]

  def onFailure[T, S >: T](value:Out[T])(f:NonEmptyList[E] => Out[S]):Out[S]
}
