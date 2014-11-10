package org.higherState.cqrs

trait Message

trait MessageReceiver[Out[+_], M <: Message] {

  def handle:Function[M, Out[Any]]

}