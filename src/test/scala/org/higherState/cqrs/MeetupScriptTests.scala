package org.higherState.cqrs

import org.scalatest.{BeforeAndAfter, Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import scalaz._
import org.higherState.cqrs.std.{Id, Transforms}
import scala.concurrent.Future

class MeetupScriptTests extends FunSuite with Matchers with ScalaFutures with BeforeAndAfter {
  import Transforms._

  //Define a Monadic service
  class MyService[Out[+_]](implicit val monad:Monad[Out]) {
    def doSomething:Out[String] =
      monad.point("Something done")
  }
  type Valid[+T] = ValidationNel[String, T]
  type Dis[+T] = \/[String, T]


  test("MyService works") {
    new MyService[Id].doSomething should equal ("Something done")
    new MyService[Valid].doSomething should equal (Success("Something done"))
    new MyService[Dis].doSomething should equal (\/-("Something done"))
  }






  class MyServiceAggregator[Out[+_], In[+_]](myService:MyService[In])(implicit val pipe: ~>[In, Out]) {

    def callDoSomething:Out[String] =
      pipe(myService.doSomething)
  }


  test("MyService aggregator") {
    new MyServiceAggregator[Valid, Id](new MyService[Id]).callDoSomething should equal (Success("Something done"))
  }








  class ExtendedServiceAggregator[Out[+_], In[+_]](myService:MyService[In])(implicit val monad:Monad[Out], val pipe: ~>[In, Out]) {

    def extendedSomething:Out[String] =
      monad.map(pipe(myService.doSomething)) { s =>
        s + " completely"
      }
  }

  test ("Myservice extender") {
    new ExtendedServiceAggregator[Valid, Id](new MyService[Id]).extendedSomething should equal (Success("Something done completely"))

    type R[+T] = Reader[String, T]
    type RValid[+T] = R[Valid[T]]
    new ExtendedServiceAggregator[RValid, R](new MyService[R]).extendedSomething.apply("Not used") should equal (Success("Something done completely"))
  }






  class MyFailableService {

    def success:Valid[String] = Success("Success")

    def failed:Valid[String] = Failure(NonEmptyList("Failed"))
  }


  class MultiplePipes[Out[+_], In[+_]](myService:MyService[In], failableService:MyFailableService)(implicit val monad:Monad[Out], val pipe1: ~>[In, Out], val pipe2: ~>[Valid, Out]) {

    def completeSucceed:Out[String] = {
      monad.bind(pipe1(myService.doSomething)) { s =>
        monad.map(pipe2(failableService.success)) { s2 =>
          s2 + ":" + s
        }
      }
    }

    def completeFailed:Out[String] = {
      monad.bind(pipe1(myService.doSomething)) { s =>
        monad.map(pipe2(failableService.failed)) { s2 =>
          s2 + ":" + s
        }
      }
    }
  }

  test("Multiple pipes") {
    val service = new MultiplePipes[Valid, Id](new MyService[Id], new MyFailableService)
    service.completeSucceed should equal (Success("Success:Something done"))
    service.completeFailed should equal (Failure(NonEmptyList("Failed")))
  }





  type FutureValid[+T] = Future[Valid[T]]
  test("Simplified service") {
    import scala.concurrent.ExecutionContext.Implicits.global

    val s = new SimplifiedMultiplePipes[FutureValid, Valid, Future](new SourceService[Valid], new SourceService[Future])
    whenReady(s.runBind){_ should equal (Success("Result,Result"))}
    whenReady(s.runSequence(3)){_ should equal (Success("Result,Result,Result,Result"))}
  }

}

class SourceService[Out[+_]:Monad] extends MonadBound[Out] {
  def doSomething:Out[String] =
    point("Result")
}

class SimplifiedMultiplePipes[Out[+_]:Monad, In1[+_]:(~>![Out])#I, In2[+_]:(~>![Out])#I]
  (myService:SourceService[In1], myService2:SourceService[In2]) extends MonadBound[Out] {

  import Scalaz._
  import ServicePipe._

  def runBind:Out[String] = {
    bind(myService.doSomething) { s =>
      map(myService2.doSomething) { s2 =>
        s2 + "," + s
      }
    }
  }

  def runSingle:Out[String] =
    myService2.doSomething

  def runSequence(c:Int):Out[String] = {
    val seq = sequence((0 to c).toList.map(_ => ~>(myService.doSomething)))
    map(seq) { s =>
      s.mkString(",")
    }
  }
}
