package org.higherState.cqrs

trait Validator {

  type M <: Message

  def validate:PartialFunction[M, List[ValidationFailure]]
}
