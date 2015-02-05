package org.higherState.authentication

trait ValidationFailure

case class ValidationException(failure:ValidationFailure) extends Throwable