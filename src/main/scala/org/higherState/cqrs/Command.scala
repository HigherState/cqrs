package org.higherState.cqrs

trait Command extends Message

trait CommandHandler {

  type C <: Command

  type R[+T]

  def handle:Function[C, R[Unit]]
}