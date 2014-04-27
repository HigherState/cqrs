package org.higherState.cqrs.pipes

import org.scalatest.{Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import org.higherState.cqrs._
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import akka.actor.ActorSystem
import akka.util.Timeout

class PipesTests extends FunSuite with Matchers with ScalaFutures {

  import scala.concurrent.duration._

  test("Double pipes") {

    val service = new MapService with IdentityCqrs {

      val left = new mutable.HashMap[Int, String] with MapDataService with Output.Identity
      val right = new mutable.HashMap[Int, String] with MapDataService with Output.Identity

      def query = new DoubleMapQuery with IdentityDirectives {

        def leftPipe = new WiredService[MapDataService] with IdentityPipe {
          def service = left
        }

        def rightPipe = new WiredService[MapDataService] with IdentityPipe {
          def service = right
        }
      }

      def commandHandler = new DoubleMapCommandHandler with IdentityDirectives {

        def leftPipe = new WiredService[MapDataService] with IdentityPipe {
          def service = left
        }

        def rightPipe = new WiredService[MapDataService] with IdentityPipe {
          def service = right
        }
      }
    }

    service.put(4,"four")
    service.put(6,"five")
    service.get(4) should equal(Some("four"))
    service.values should equal(List("four", "five"))
  }

  test("ActorFuture double pipes") {
    implicit val system = ActorSystem("System")

    val service = new MapService with AkkaCqrs {

      implicit def timeout: Timeout = 45.seconds
      implicit def executionContext: ExecutionContext = system.dispatcher

      val left = new FutureMapDataService
      val right = new FutureMapDataService

      protected val query =
        getQueryRef("DoubleMap") { excctx =>
          new DoubleMapQuery with ActorWrapper with FutureDirectives {

            implicit def executionContext: ExecutionContext = excctx

            def leftPipe = new WiredService[MapDataService] with FuturePipe {

              implicit def executionContext: ExecutionContext = excctx
              def service = left
            }

            def rightPipe = new WiredService[MapDataService] with FuturePipe {

              implicit def executionContext: ExecutionContext = excctx
              def service = right
            }
          }
        }

      protected val commandHandler =
        getCommandHandlerRef("DoubleMap") { excctx =>
          new DoubleMapCommandHandler with ActorWrapper with FutureDirectives {

            implicit def executionContext: ExecutionContext = excctx

            def leftPipe = new WiredService[MapDataService] with FuturePipe {

              implicit def executionContext: ExecutionContext = excctx
              def service = left
            }

            def rightPipe = new WiredService[MapDataService] with FuturePipe {

              implicit def executionContext: ExecutionContext = excctx
              def service = right
            }
          }
        }
    }

    whenReady(service.put(4, "four")) { u =>
      whenReady(service.put(6, "five")) { u =>
        whenReady(service.get(4)) {s =>
          s should equal(Some("four"))
          whenReady(service.values) {l =>
            l should equal(List("four", "five"))
          }
        }
      }
    }
  }
}
