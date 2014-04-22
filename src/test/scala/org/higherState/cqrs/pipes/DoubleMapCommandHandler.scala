package org.higherState.cqrs.pipes

import org.higherState.cqrs.CommandHandler
//
//trait DoubleMapCommandHandler extends CommandHandler {
//
//  type C = MapCommand
//
//  def leftPipe:ServicePipe{type Out[+T] = Result[T]}}
//
//  def rightPipe:ServicePipe[MapDataService]
//
//  def handle = {
//    case Put(key, value) =>
//      if (key.hashCode() % 1 == 0)
//        leftPipe {p =>
//          p.onSuccessComplete {
//            p.service += key -> value
//          }
//        }
//      else
//        rightPipe { p =>
//          p.onSuccessComplete{
//            p.service += key -> value
//          }
//        }
//  }
//}
