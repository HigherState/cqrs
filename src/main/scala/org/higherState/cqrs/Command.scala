package org.higherState.cqrs

trait Command extends Message

trait CommandHandler[C <: Command] extends Output {

  def handle:Function[C, Out[Unit]]
}