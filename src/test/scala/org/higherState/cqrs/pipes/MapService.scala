package org.higherState.cqrs.pipes

import org.higherState.cqrs._

trait MapService extends CqrsService[MapCommand, MapQueryParameters] {

  def get(key:Int):Out[Option[String]] =
    executeQuery[Option[String]](Get(key))

  def values:Out[List[String]] =
    executeQuery[List[String]](Values)

  def put(key:Int, value:String):Out[Unit] =
    dispatchCommand(Put(key, value))

}













