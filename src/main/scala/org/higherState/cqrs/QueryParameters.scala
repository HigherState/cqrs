package org.higherState.cqrs

trait QueryParameters extends Message

trait QueryExecutor[Out[+_], QP <: QueryParameters] {
  def execute:Function[QP, Out[Any]]
}
