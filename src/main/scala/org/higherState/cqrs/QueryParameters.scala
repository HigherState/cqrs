package org.higherState.cqrs

trait QueryParameters extends Message

trait Query extends Output {

  type QP <: QueryParameters

  def execute:Function[QP, Out[Any]]
}