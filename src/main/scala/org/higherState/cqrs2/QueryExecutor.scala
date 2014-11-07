package org.higherState.cqrs2

import org.higherState.cqrs.QueryParameters


trait QueryExecutor[Out[+_], QP <: QueryParameters] {

  def execute:Function[QP, Out[Any]]
}
