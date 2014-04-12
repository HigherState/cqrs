package org.higherState.cqrs.pipes

import org.higherState.cqrs.CommandHandler

trait MapCommandHandler extends CommandHandler with Pipe {

  def service:MapDataService{type R[+T]= In[T]}

  type R[+T] = Out[T]
  type C = MapCommand

  def handle = {
    case Put(key, value) =>
      onSuccessComplete {
        service += key -> value
      }
  }
}
