package org.higherState.cqrs

import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import org.scalatest.{Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import scala.collection.mutable

class MapTests extends FunSuite with Matchers with ScalaFutures {

  import scala.concurrent.duration._

  test("MapIdentityService") {
    MapIdentityService.put(4,"four")
    MapIdentityService.put(6,"five")
    MapIdentityService.get(4) should equal(Some("four"))
    MapIdentityService.values should equal(List("four", "five"))
  }

  test("ActorService") {
    implicit val system = ActorSystem("System")
    implicit val timeout:Timeout = 45.seconds
    implicit def executionContext: ExecutionContext = system.dispatcher

    val service = MapAkkaService()
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

  test("ActorFutureService") {
    implicit val system = ActorSystem("System")
    implicit val timeout:Timeout = 45.seconds
    implicit def executionContext: ExecutionContext = system.dispatcher

    val service = MapAkkaFutureService()
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
