package org.higherState.cqrs.pipes

import org.higherState.cqrs.{Output, Service}
import scala.concurrent.{Future, ExecutionContext}
import scala.collection.mutable

trait MapDataService extends Service {

  def get(key:Int):Out[Option[String]]

  def values:Out[TraversableOnce[String]]

  def +=(kv: (Int, String)):Out[this.type]
}

class FutureMapDataService(implicit executionContext:ExecutionContext) extends MapDataService with Output.Future {
  val state = new mutable.HashMap[Int,String]()

  def get(key: Int): Future[Option[String]] =
    Future(state.get(key))

  def values: Future[TraversableOnce[String]] =
    Future(state.values)

  def +=(kv: (Int, String)):Future[this.type] =
    Future(state += kv).map(_ => this)
}
