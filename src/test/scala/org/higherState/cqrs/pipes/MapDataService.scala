package org.higherState.cqrs.pipes

import org.higherState.cqrs.Service
import scala.concurrent.{Future, ExecutionContext}
import scala.collection.mutable

trait MapDataService[R[+_]] extends Service[R] {

  def get(key:Int):R[Option[String]]

  def values:R[TraversableOnce[String]]

  def +=(kv: (Int, String)):R[this.type]
}

class FutureMapDataService(implicit executionContext:ExecutionContext) extends MapDataService[Future] {
  val state = new mutable.HashMap[Int,String]()

  def get(key: Int): Future[Option[String]] =
    Future(state.get(key))

  def values: Future[TraversableOnce[String]] =
    Future(state.values)

  def +=(kv: (Int, String)):Future[this.type] =
    Future(state += kv).map(_ => this)
}
