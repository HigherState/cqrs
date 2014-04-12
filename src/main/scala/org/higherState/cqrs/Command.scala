package org.higherState.cqrs

trait Command extends Message

trait CommandHandler extends Output {

  type C <: Command

  def handle:Function[C, Out[Unit]]
}