package org.higherState.cqrs

import org.scalatest.{Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures


class NestedTraitTest extends FunSuite with Matchers with ScalaFutures {

  test("nested trait") {
    val impl = new Test1 {

      def three = new Level2 {
        def get = 3
      }
      def four = new Level2 {
        def get = 4
      }
    }
    impl.three.get should equal(3)
  }
}

trait Test1 extends Level1 {

  def three:Level2

  def four:Level2
}

trait Level1 {

  trait Level2 {
    def get:Int
  }
}
