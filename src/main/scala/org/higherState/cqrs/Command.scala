package org.higherState.cqrs

trait Command

trait CommandHandler[Out[+_], C <: Command] {

  def handle:Function[C, Out[Unit]]
}