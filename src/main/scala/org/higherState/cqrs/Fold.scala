package org.higherState.cqrs


trait Fold[Out[+_]] {

  def apply[T](value:Out[T])(f:T => Unit):Unit
}



