package org.higherState.cqrs

trait Logger {

  type M <: Message

  def log:PartialFunction[M, Unit]
}
