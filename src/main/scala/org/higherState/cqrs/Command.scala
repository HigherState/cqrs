package org.higherState.cqrs

trait Command

trait CommandHandler[Out[+_], C <: Command] {

  def handle:Function[C, Out[Ack]]

  def acknowledged(implicit m:Monad[Out]) =
    m.point(Acknowledged)
}

trait Acknowledged
object Acknowledged extends Acknowledged