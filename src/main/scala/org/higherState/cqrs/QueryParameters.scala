package org.higherState.cqrs

trait QueryParameters extends Message

trait Query {

  type QP <: QueryParameters

  type QR[+T]

  def execute:Function[QP, QR[Any]]
}