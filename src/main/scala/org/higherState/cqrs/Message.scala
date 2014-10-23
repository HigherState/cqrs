package org.higherState.cqrs

trait Message

trait MessageReceiver[M <: Message] extends Output {

  def handle:Function[M, Out[Any]]

}