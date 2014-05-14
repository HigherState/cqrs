package org.higherState.cqrs

import akka.actor.{ActorRef, ActorRefFactory}
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait MapService extends CqrsService[MapCommand, MapQueryParameters] {

  def get(key:Int):Out[Option[String]] =
    executeQuery[Option[String]](Get(key))

  def values:Out[List[String]] =
    executeQuery[List[String]](Values)

  def put(key:Int, value:String):Out[Unit] =
    dispatchCommand(Put(key, value))
}

sealed trait MapQueryParameters extends QueryParameters

case class Get(key:Int) extends MapQueryParameters

case object Values extends MapQueryParameters



sealed trait MapCommand extends Command

case class Put(key:Int, value:String) extends MapCommand

trait MapDataService extends Service {

  def get(key:Int):Out[Option[String]]

  def values:Out[TraversableOnce[String]]

  def contains(key:Int):Out[Boolean]

  def +=(kv: (Int, String)):Out[this.type]

  def -=(key:Int):Out[this.type]
}

trait MapCommandHandler extends CommandHandler[MapCommand] with ServicePipeDirectives[MapDataService] {

  def handle = {
    case Put(key, value) =>
      onSuccess {
        service += key -> value
      } { s => complete}
  }
}

trait MapQuery extends Query[MapQueryParameters] with ServicePipeDirectives[MapDataService] {

  def execute = {
    case Get(key) =>
      success(service.get(key))
    case Values =>
      onSuccess(service.values){l =>
        result(l.toList)
      }
  }
}

trait DoubleMapDirectives extends ServicePipesDirectives {

  def leftPipe:ServicePipeDirectives[MapDataService]

  def rightPipe:ServicePipeDirectives[MapDataService]

  def withPipe[T](key:Int)(f:ServicePipeDirectives[MapDataService] => Out[T]) =
    if (key.hashCode() % 2 == 0)
      f(leftPipe)
    else
      f(rightPipe)
}

trait DoubleMapCommandHandler extends CommandHandler[MapCommand] with DoubleMapDirectives {

  def handle = {
    case Put(key, value) =>
      withPipe(key) { p =>
        p.onSuccessComplete {
          p.service += key -> value
        }
      }
  }
}

trait DoubleMapQuery extends Query[MapQueryParameters] with DoubleMapDirectives {

  def leftPipe:ServicePipeDirectives[MapDataService]

  def rightPipe:ServicePipeDirectives[MapDataService]

  def execute = {
    case Get(key) =>
      withPipe(key) { p =>
        p.success(p.service.get(key))
      }
    case Values =>
      merge(
        leftPipe{ p =>
          p.success(p.service.values)
        },
        rightPipe{ p =>
          p.success(p.service.values)
        }){(l:TraversableOnce[String],r:TraversableOnce[String]) =>
        result((l.toIterator ++ r.toIterator).toList)
      }

  }
}

case object MapIdentityService extends MapService with IdentityCqrs {

  val state = new mutable.HashMap[Int,String] with MapDataService with Output.Identity

  def query = new MapQuery with IdentityPipeDirectives with IdentityDirectives {
    def service = state
  }

  def commandHandler = new MapCommandHandler with IdentityPipeDirectives with IdentityDirectives {
    def service = state
  }
}

case object DoubleMapIdentityService extends MapService with IdentityCqrs {

  val left = new mutable.HashMap[Int, String] with MapDataService with Output.Identity
  val right = new mutable.HashMap[Int, String] with MapDataService with Output.Identity

  def query = new DoubleMapQuery with IdentityDirectives {

    def leftPipe = new ServicePipeDirectives[MapDataService] with IdentityPipeDirectives with IdentityDirectives {
      def service = left
    }

    def rightPipe = new ServicePipeDirectives[MapDataService] with IdentityPipeDirectives with IdentityDirectives {
      def service = right
    }
  }

  def commandHandler = new DoubleMapCommandHandler with IdentityDirectives {

    def leftPipe = new ServicePipeDirectives[MapDataService] with IdentityPipeDirectives with IdentityDirectives {
      def service = left
    }

    def rightPipe = new ServicePipeDirectives[MapDataService] with IdentityPipeDirectives with IdentityDirectives {
      def service = right
    }
  }
}


case class MapAkkaService(implicit factory:ActorRefFactory, timeout:akka.util.Timeout, executionContext:ExecutionContext) extends MapService with Output.Future with AkkaCqrs {

  val state = new mutable.HashMap[Int,String] with MapDataService with Output.Identity

  protected val commandHandler: ActorRef =
    getCommandHandlerRef("Map") { excctx =>
      new MapCommandHandler with ActorAdapter with IdentityPipeDirectives with IdentityDirectives {
        def service = state

        implicit def executionContext: ExecutionContext = excctx
      }
    }

  protected val query: ActorRef =
    getQueryRef("Map") { excctx =>
      new MapQuery with ActorAdapter with IdentityPipeDirectives with IdentityDirectives {
        def service = state

        implicit def executionContext: ExecutionContext = excctx
      }
    }
}

class FutureMapDataService(implicit executionContext:ExecutionContext) extends MapDataService with Output.Future {
  val state = new mutable.HashMap[Int,String]()

  def get(key: Int): Future[Option[String]] =
    Future(state.get(key))

  def values: Future[TraversableOnce[String]] =
    Future(state.values)

  def contains(key:Int):Future[Boolean] =
    Future(state.contains(key))

  def +=(kv: (Int, String)):Future[this.type] =
    Future(state += kv).map(_ => this)

  def -=(key:Int):Future[this.type] =
    Future(state -= key).map(_ => this)
}

case class MapAkkaFutureService(implicit factory:ActorRefFactory, timeout:akka.util.Timeout, executionContext:ExecutionContext) extends MapService with Output.Future with AkkaCqrs {

  val futureService = new FutureMapDataService

  protected val commandHandler: ActorRef =
    getCommandHandlerRef("FutureMap") { excctx =>
      new MapCommandHandler with ActorAdapter with FuturePipeDirectives {
        def service = futureService

        implicit def executionContext: ExecutionContext = excctx
      }
    }

  protected val query: ActorRef =
    getQueryRef("FutureMap") { excctx =>
      new MapQuery with ActorAdapter with FuturePipeDirectives {
        def service = futureService

        implicit def executionContext: ExecutionContext = excctx
      }
    }
}

case class DoubleMapAkkaFutureService(implicit factory:ActorRefFactory, timeout:akka.util.Timeout, executionContext:ExecutionContext) extends  MapService with AkkaCqrs {

  val left = new FutureMapDataService
  val right = new FutureMapDataService

  protected val query =
    getQueryRef("DoubleMap") { excctx =>
      new DoubleMapQuery with ActorAdapter with FutureDirectives {

        implicit def executionContext: ExecutionContext = excctx

        def leftPipe = new ServicePipeDirectives[MapDataService] with FuturePipeDirectives {

          implicit def executionContext: ExecutionContext = excctx
          def service = left
        }

        def rightPipe = new ServicePipeDirectives[MapDataService] with FuturePipeDirectives {

          implicit def executionContext: ExecutionContext = excctx
          def service = right
        }
      }
    }

  protected val commandHandler =
    getCommandHandlerRef("DoubleMap") { excctx =>
      new DoubleMapCommandHandler with ActorAdapter with FutureDirectives {

        implicit def executionContext: ExecutionContext = excctx

        def leftPipe = new ServicePipeDirectives[MapDataService] with FuturePipeDirectives {

          implicit def executionContext: ExecutionContext = excctx
          def service = left
        }

        def rightPipe = new ServicePipeDirectives[MapDataService] with FuturePipeDirectives {

          implicit def executionContext: ExecutionContext = excctx
          def service = right
        }
      }
    }
}


object State {
  val map = mutable.Map.empty[Int, String]
}