package org.higherState.cqrs.pipes

import org.higherState.cqrs.CommandHandler

trait MapCommandHandler extends CommandHandler with Pipe {

  def service:MapDataService[In]

  type C = MapCommand

  def handle = {
    case Put(key, value) =>
      onSuccessComplete {
        service += key -> value
      }
  }
}
