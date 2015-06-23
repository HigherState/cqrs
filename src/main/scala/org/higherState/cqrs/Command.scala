package org.higherState.cqrs

trait Command extends Serializable

trait CommandHandler[Out[+_], C <: Command] {

  def handle:Function[C, Out[Ack]]

  def acknowledged(implicit m:Monad[Out]):Out[Acknowledged] =
    m.point(Acknowledged)
}

trait Acknowledged extends Serializable
object Acknowledged extends Acknowledged