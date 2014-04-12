package org.higherState.cqrs.pipes

import org.higherState.cqrs.Query
import org.higherState.cqrs.directives.Directives

trait DoubleMapQuery extends Query with Directives {

  type QP = MapQueryParameters
  type QR[+T] = Out[T]

  def leftPipe:ServicePipe[MapDataService]{type Out[T] = QR[T]}

  def rightPipe:ServicePipe[MapDataService]{type Out[T] = QR[T]}

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
