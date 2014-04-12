package org.higherState.cqrs

trait Command extends Message

trait CommandHandler {

  type C <: Command

  type CR[+T]

  def handle:Function[C, CR[Unit]]
}