package org.higherState.cqrs

import scalaz.{NonEmptyList, Monad}

//TODO: Better name
trait Validator[E, Out[+_]] extends Monad[Out] {

  def failure(validationFailure: => E):Out[Nothing]

  def failures(validationFailures: => NonEmptyList[E]):Out[Nothing]
}
