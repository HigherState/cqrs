package org.higherState.cqrs

trait QueryParameters extends Message

trait QueryExecutor[QP <: QueryParameters] extends Output {

  def execute:Function[QP, Out[Any]]
}