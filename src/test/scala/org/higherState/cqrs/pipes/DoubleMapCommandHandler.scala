package org.higherState.cqrs.pipes

import org.higherState.cqrs.{Pipes, CommandHandler}

trait DoubleMapDirectives extends Pipes {

  def leftPipe:PipedService[MapDataService]

  def rightPipe:PipedService[MapDataService]

  def withPipe[T](key:Int)(f:PipedService[MapDataService] => Out[T]) =
    if (key.hashCode() % 2 == 0)
      f(leftPipe)
    else
      f(rightPipe)
}

trait DoubleMapCommandHandler extends CommandHandler[MapCommand] with DoubleMapDirectives {

  def handle = {
    case Put(key, value) =>
      withPipe(key) { p =>
        p.onSuccessComplete {
          p.service += key -> value
        }
      }
  }
}
