package org.higherState.cqrs

trait QueryParameters extends Message

trait Query {

  type QP <: QueryParameters

  type R[+T]

  def execute:Function[QP, R[Any]]
}