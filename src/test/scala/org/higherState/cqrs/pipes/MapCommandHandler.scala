package org.higherState.cqrs.pipes

import org.higherState.cqrs.{PipedService, CommandHandler}

trait MapCommandHandler extends CommandHandler[MapCommand] with PipedService[MapDataService] {

  type C = MapCommand

  def handle = {
    case Put(key, value) =>
      onSuccessComplete {
        service += key -> value
      }
  }
}
