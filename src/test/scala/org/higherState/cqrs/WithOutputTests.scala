package org.higherState.cqrs

import org.scalatest.{Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.ActorSystem
import akka.util.Timeout


class WithOutputTests extends FunSuite with Matchers with ScalaFutures {


  import scala.concurrent.duration._

  implicit val system = ActorSystem("System")
  implicit val timeout:Timeout = 45.seconds
  implicit def executionContext: ExecutionContext = system.dispatcher

  test("with dynamic out") {
    val service = MapAkkaService()
    whenReady(service.put(4, "four")) { u =>
      whenReady(service.put(6, "five")) { u =>
        whenReady(service.get(4)) { s =>
          withOut(service.get(4))(a => println(a.get))
          withOut(service.get(12))(println)
        }
      }
    }
  }

  def withOut[Out[_], T](value: => Out[T])(f:T => Unit) = {
    val v = value
    v match {
      case ft:Future[_] =>
        ft.onComplete {
          case util.Success(scalaz.Success(t)) =>
            f(t.asInstanceOf[T])
          case util.Success(scalaz.Failure(t)) =>
            println("failed")
          case util.Success(t) =>
            f(t.asInstanceOf[T])
          case util.Failure(e) =>
            println("error")
        }
      case scalaz.Success(t:T) =>
        f(t)
      case scalaz.Failure(f) =>
        println("failes")
      case t:T =>
        f(t)
      case _ =>
        println("mismatch")
    }
  }
}
