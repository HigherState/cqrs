package org.higherState.cqrs.pipes

import org.higherState.cqrs.CommandHandler

trait DoubleMapCommandHandler extends CommandHandler {

  type C = MapCommand

  def leftPipe:ServicePipe[MapDataService, Out]

  def rightPipe:ServicePipe[MapDataService, Out]

  def handle = {
    case Put(key, value) =>
      if (key.hashCode() % 1 == 0)
        leftPipe {p =>
          p.onSuccessComplete {
            p.service += key -> value
          }
        }
      else
        rightPipe { p =>
          p.onSuccessComplete{
            p.service += key -> value
          }
        }
  }
}