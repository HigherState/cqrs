package org.higherState.cqrs

import _root_.akka.actor.{ActorRef, ActorRefFactory}
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait MapService[R[_]] extends CqrsService[R, MapCommand, MapQueryParameters] {

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

trait MapDataService[R[+_]] extends Service[R] {

  def get(key:Int):R[Option[String]]

  def values:R[TraversableOnce[String]]

  def +=(kv: (Int, String)):R[this.type]
}

trait MapCommandHandler extends CommandHandler[MapCommand] with PipedService[MapDataService] {

  def handle = {
    case Put(key, value) =>
      onSuccess {
        service += key -> value
      } { s => complete}
  }
}

trait MapQuery extends Query[MapQueryParameters] with PipedService[MapDataService] {

  def execute = {
    case Get(key) =>
      success(service.get(key))
    case Values =>
      onSuccess(service.values){l =>
        result(l.toList)
      }
  }
}

case object MapIdentityService extends MapService[Identity] with IdentityCqrs {

  val state = new mutable.HashMap[Int,String] with MapDataService[Identity]

  def query = new MapQuery with IdentityPipe {
    def service = state
  }

  def commandHandler = new MapCommandHandler with IdentityPipe {
    def service = state
  }
}

case class MapAkkaService(implicit factory:ActorRefFactory, timeout:_root_.akka.util.Timeout, executionContext:ExecutionContext) extends MapService[Future] with AkkaCqrs {

  val state = new mutable.HashMap[Int,String] with MapDataService[Identity]

  protected val commandHandler: ActorRef =
    getCommandHandlerRef("Map") { excctx =>
      new MapCommandHandler with ActorWrapper with IdentityPipe {
        def service = state

        implicit def executionContext: ExecutionContext = excctx
      }
    }

  protected val query: ActorRef =
    getQueryRef("Map") { excctx =>
      new MapQuery with ActorWrapper with IdentityPipe {
        def service = state

        implicit def executionContext: ExecutionContext = excctx
      }
    }
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

case class MapAkkaFutureService(implicit factory:ActorRefFactory, timeout:_root_.akka.util.Timeout, executionContext:ExecutionContext) extends MapService[Future] with AkkaCqrs {

  val futureService = new FutureMapDataService

  protected val commandHandler: ActorRef =
    getCommandHandlerRef("Map") { excctx =>
      new MapCommandHandler with ActorWrapper with FuturePipe {
        def service = futureService

        implicit def executionContext: ExecutionContext = excctx
      }
    }

  protected val query: ActorRef =
    getQueryRef("Map") { excctx =>
      new MapQuery with ActorWrapper with FuturePipe {
        def service = futureService

        implicit def executionContext: ExecutionContext = excctx
      }
    }
}


object State {
  val map = mutable.Map.empty[Int, String]
}