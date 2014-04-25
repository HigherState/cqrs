package org.higherState.cqrs

trait QueryParameters extends Message

trait Query[QP <: QueryParameters] extends Output {

  def execute:Function[QP, Out[Any]]
}