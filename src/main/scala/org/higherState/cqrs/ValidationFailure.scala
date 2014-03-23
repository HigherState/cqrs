package org.higherState.cqrs

trait ValidationFailure

case class ValidationException(failure:ValidationFailure) extends Throwable