package org.higherState.cqrs

trait Command extends Message

trait CommandHandler[Out[+_], C <: Command] {

  def handle:Function[C, Out[Unit]]
}