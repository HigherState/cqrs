package org.higherState.cqrs

trait Message[R] extends Serializable

trait MessageReceiver[Out[+_], M[_] <: Message[_]] {

  def handle[T]:Function[M[T], Out[T]]

}