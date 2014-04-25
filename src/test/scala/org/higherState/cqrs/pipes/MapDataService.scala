package org.higherState.cqrs.pipes

import org.higherState.cqrs.Service

trait MapDataService[R[+_]] extends Service[R] {

  def get(key:Int):R[Option[String]]

  def values:R[TraversableOnce[String]]

  def +=(kv: (Int, String)):R[this.type]
}
