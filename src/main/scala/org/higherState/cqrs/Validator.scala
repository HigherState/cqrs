package org.higherState.cqrs

import scalaz.{NonEmptyList, Monad}

//TODO: Better name
trait Validator[Out[+_]] extends Monad[Out] {

  def failure(validationFailure: => ValidationFailure):Out[Nothing]

  def failures(validationFailures: => NonEmptyList[ValidationFailure]):Out[Nothing]
}
