package org.higherState.cqrs

import org.scalatest.{Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import scalaz._
import Scalaz._


class TransformerTests extends FunSuite with Matchers with ScalaFutures  {

  type Error[+A] = \/[String, A]
  type Result[+A] = OptionT[Error, A]

  test("hi") {
    val result: Result[Int] = 42.point[Result]
    val result2 = OptionT(none[Int].point[Error])
    val result3 = OptionT("Error message".left : Error[Option[Int]])
    result.map(println)
  }

  type ExceptionsOr[A] = ValidationNel[Exception, A]

  test("sequent") {
    val results: Seq[ExceptionsOr[Int]] = Seq(
      "13".parseInt.liftFailNel, "42".parseInt.liftFailNel
    )
    results.sequence.map(i => println(i))
  }
}
