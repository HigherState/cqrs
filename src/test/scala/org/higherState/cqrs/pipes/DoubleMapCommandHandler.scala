package org.higherState.cqrs.pipes

import org.higherState.cqrs.{WiredPipes, CommandHandler}

trait DoubleMapCommandHandler extends CommandHandler[MapCommand] with WiredPipes {

  def leftPipe:WiredService[MapDataService]

  def rightPipe:WiredService[MapDataService]

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
