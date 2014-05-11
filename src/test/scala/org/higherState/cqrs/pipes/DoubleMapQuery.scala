package org.higherState.cqrs.pipes

import org.higherState.cqrs.Query

trait DoubleMapQuery extends Query[MapQueryParameters] with DoubleMapDirectives {

  def leftPipe:PipedService[MapDataService]

  def rightPipe:PipedService[MapDataService]

  def execute = {
    case Get(key) =>
      withPipe(key) { p =>
        p.success(p.service.get(key))
      }
    case Values =>
      merge(
        leftPipe{ p =>
          p.success(p.service.values)
        },
        rightPipe{ p =>
          p.success(p.service.values)
        }){(l:TraversableOnce[String],r:TraversableOnce[String]) =>
        result((l.toIterator ++ r.toIterator).toList)
      }

  }
}
