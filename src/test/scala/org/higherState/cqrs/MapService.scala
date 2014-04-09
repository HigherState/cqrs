package org.higherState.cqrs

import _root_.akka.actor.{ActorRef, ActorRefFactory}
import org.higherState.cqrs.identity.IdentityCqrsService
import scala.collection.mutable
import org.higherState.cqrs.akka.{AkkaCqrsService, ActorWrapper}
import scala.concurrent.{ExecutionContext, Future}
import org.higherState.cqrs.pipes.{FuturePipe, IdentityPipe, Pipe}

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

sealed trait MapQueryParameters extends QueryParameters

case class Get(key:Int) extends MapQueryParameters

case object Values extends MapQueryParameters



sealed trait MapCommand extends Command
case class Put(key:Int, value:String) extends MapCommand

trait MapDataService extends Service {

  def get(key:Int):R[Option[String]]

  def values:R[TraversableOnce[String]]

  def +=(kv: (Int, String)):R[this.type]
}

trait MapCommandHandler extends CommandHandler with Pipe {

  def service:MapDataService{type R[T]= Out[T]}

  type C = MapCommand

  def handle = {
    case Put(key, value) =>
      onSuccess {
        service += key -> value
      } { complete}
  }
}

trait MapQuery extends Query with Pipe {
  q =>
  def service:MapDataService{type R[T] = Out[T]}

  type QP = MapQueryParameters

  def execute = {
    case Get(key) =>
      success(service.get(key))
    case Values =>
      onSuccess(service.values){l =>
        result(l.toList)
      }
  }
}

case object MapIdentityService extends MapService with IdentityCqrsService {

  val state = new mutable.HashMap[Int,String] with MapDataService with Repository

  def query = new MapQuery with IdentityPipe {
    def service = state
  }

  def commandHandler = new MapCommandHandler with IdentityPipe {
    def service = state
  }
}

case class MapAkkaService(implicit factory:ActorRefFactory, timeout:_root_.akka.util.Timeout, executionContext:ExecutionContext) extends MapService with AkkaCqrsService {
  parent =>
  val state = new mutable.HashMap[Int,String] with MapDataService with Repository

  protected val commandHandler: ActorRef =
    getCommandHandlerRef("Map") {
      new MapCommandHandler with ActorWrapper with IdentityPipe {
        def service = state

        implicit def executionContext: ExecutionContext = parent.executionContext
      }
    }

  protected val query: ActorRef =
    getQueryRef("Map") {
      new MapQuery with ActorWrapper with IdentityPipe {
        def service = state

        implicit def executionContext: ExecutionContext = parent.executionContext
      }
    }
}

class FutureMapDataService(implicit executionContext:ExecutionContext) extends MapDataService {
  val state = new mutable.HashMap[Int,String]()

  type R[+T] = Future[T]

  def get(key: Int): R[Option[String]] =
    Future(state.get(key))

  def values: R[TraversableOnce[String]] =
    Future(state.values)

  def +=(kv: (Int, String)):Future[this.type] =
    Future(state += kv).map(_ => this)
}

case class MapAkkaFutureService(implicit factory:ActorRefFactory, timeout:_root_.akka.util.Timeout, executionContext:ExecutionContext) extends MapService with AkkaCqrsService {
  parent =>
  val futureService = new FutureMapDataService

  protected val commandHandler: ActorRef =
    getCommandHandlerRef("Map") {
      new MapCommandHandler with ActorWrapper with FuturePipe {
        def service = futureService

        implicit def executionContext: ExecutionContext = parent.executionContext
      }
    }

  protected val query: ActorRef =
    getQueryRef("Map") {
      new MapQuery with ActorWrapper with FuturePipe {
        def service = futureService

        implicit def executionContext: ExecutionContext = parent.executionContext
      }
    }
}


object State {
  val map = mutable.Map.empty[Int, String]
}