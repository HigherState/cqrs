package org.higherState.cqrs.pipes

import org.higherState.cqrs.Query
import org.higherState.cqrs.directives.Directives

//trait DoubleMapQuery extends Query with Directives {
//
//  type QP = MapQueryParameters
//
//  def leftPipe:ServicePipe[MapDataService, Out]
//
//  def rightPipe:ServicePipe[MapDataService, Out]
//
//  def execute = {
//    case Get(key) =>
//      merge(
//        leftPipe{ p =>
//          p.success(p.service.get(key))
//        },
//        rightPipe {
//          p => p.success(p.service.get(key))
//        })
//        {(l,r) =>
//          result(l.orElse(r))
//        }
//    case Values =>
//      merge(
//        leftPipe{ p =>
//          p.success(p.service.values)
//        },
//        rightPipe{ p =>
//          p.success(p.service.values)
//        }){(l:TraversableOnce[String],r:TraversableOnce[String]) =>
//        result((l.toIterator ++ r.toIterator).toList)
//      }
//
//  }
//}
