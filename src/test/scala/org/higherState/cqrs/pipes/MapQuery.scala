package org.higherState.cqrs.pipes

import org.higherState.cqrs.Query

trait MapQuery extends Query with Pipe {

  def service:MapDataService[In]

  type QP = MapQueryParameters

  def execute = {
    case Get(key) =>
      success(service.get(key))
    case Values =>
      onSuccess(service.values){l =>
        result(l.toList)
      }
  }
}
