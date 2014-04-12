package org.higherState.cqrs.pipes

import org.higherState.cqrs.Query
import org.higherState.cqrs.directives.Directives

trait DoubleMapQuery extends Query with Directives {

  type QP = MapQueryParameters

  def leftPipe:ServicePipe[MapDataService]{type Out[T] = R[T]}

  def rightPipe:ServicePipe[MapDataService]{type Out[T] = R[T]}


  def execute = {
    case Get(key) =>
      merge(
        leftPipe{ p =>
          p.success(p.service.get(key))
        },
        rightPipe {
          p => p.success(p.service.get(key))
        }){(l,r) =>
        l.orElse(r)
      }
    case Values =>
      merge(
        leftPipe{ p =>
          p.success(p.service.values)
        },
        rightPipe{ p =>
          p.success(p.service.values)
        }){(l:TraversableOnce[String],r:TraversableOnce[String]) =>
        l.toIterator ++ r.toIterator
      }

  }
}
