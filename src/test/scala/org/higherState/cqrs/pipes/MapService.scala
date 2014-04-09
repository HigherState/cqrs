package org.higherState.cqrs.pipes

import org.higherState.cqrs._

trait MapService extends CqrsService {

  type C = MapCommand
  type QP = MapQueryParameters

  def get(key:Int):R[Option[String]] =
    executeQuery[Option[String]](Get(key))

  def values:R[List[String]] =
    executeQuery[List[String]](Values)

  def put(key:Int, value:String):R[Unit] =
    dispatchCommand(Put(key, value))

}













