package org.higherState.cqrs

trait QueryParameters

trait QueryExecutor[Out[+_], QP <: QueryParameters] {
  def execute:Function[QP, Out[Any]]
}
