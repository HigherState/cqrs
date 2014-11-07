package org.higherState.cqrs2

import org.higherState.cqrs.Command

trait CommandHandler[Out[+_], C <: Command] {

  def handle:Function[C, Out[Unit]]
}