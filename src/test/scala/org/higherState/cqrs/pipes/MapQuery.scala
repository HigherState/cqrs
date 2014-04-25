package org.higherState.cqrs.pipes

import org.higherState.cqrs.{PipedService, Query}

trait MapQuery extends Query[MapQueryParameters] with PipedService[MapDataService] {

  def execute = {
    case Get(key) =>
      success(service.get(key))
    case Values =>
      onSuccess(service.values){l =>
        result(l.toList)
      }
  }
}
