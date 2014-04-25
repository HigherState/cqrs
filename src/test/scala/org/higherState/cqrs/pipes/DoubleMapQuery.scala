package org.higherState.cqrs.pipes

import org.higherState.cqrs.{WiredPipes, Query}

trait DoubleMapQuery extends Query[MapQueryParameters] with WiredPipes {

  def leftPipe:WiredService[MapDataService]

  def rightPipe:WiredService[MapDataService]

  def execute = {
    case Get(key) =>
      merge(
        leftPipe{ p =>
          p.success(p.service.get(key))
        },
        rightPipe {
          p => p.success(p.service.get(key))
        })
        {(l,r) =>
          result(l.orElse(r))
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
