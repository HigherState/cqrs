package org.higherState.cqrs.pipes

import org.higherState.cqrs.Query

trait MapQuery extends Query with Pipe {

  def service:MapDataService{type R[T] = In[T]}

  type QP = MapQueryParameters
  type R[+T] = Out[T]

  def execute = {
    case Get(key) =>
      success(service.get(key))
    case Values =>
      onSuccess(service.values){l =>
        result(l.toList)
      }
  }
}
