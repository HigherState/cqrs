package org.higherState.cqrs.pipes

import org.higherState.cqrs.CommandHandler

trait MapCommandHandler extends CommandHandler with Pipe {

  def service:MapDataService{type R[T]= In[T]}

  type C = MapCommand

  type R[+T] = Out[T]

  def handle = {
    case Put(key, value) =>
      complete {
        service += key -> value
      }

  }
}
