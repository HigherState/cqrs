package org.higherState.cqrs.pipes

import org.scalatest.{Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import org.higherState.cqrs._
import scala.collection.mutable

class PipesTests extends FunSuite with Matchers with ScalaFutures {

  test("Double pipes") {

    val service = new MapService[Identity] with IdentityCqrs {

      val left = new mutable.HashMap[Int,String] with MapDataService[Identity]
      val right = new mutable.HashMap[Int,String] with MapDataService[Identity]

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

}
