package org.higherState.cqrs

trait QueryParameters[R] extends Serializable

trait QueryExecutor[Out[+_], QP[_] <: QueryParameters[_]] {
  def execute[T]:Function[QP[T], Out[T]]
}

