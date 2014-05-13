package org.higherState.cqrs

import org.scalatest.{Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures
import scala.util.{Failure, Success, Try}

class TypeTest extends  FunSuite with Matchers with ScalaFutures {

  test("Mixin abstracted types test") {

  }
  test("Abstract type only") {
    val w = new ExtendedWired {
      def get = Some(4)

      type W[+T] = Option[T]
    }
    val j = new Join {
      type In[+T] = Option[T]
      type Out[+T] = Try[T]
      type Wire = ExtendedWired

      def convert[T](i: In[T]): Out[T] =
        i.map(Success(_)).getOrElse(Failure(new Exception("Not found")))

      def wired = w
    }

    val b = new ExtendedBase {
      type Result[+T] = Try[T]
      def join = j
    }
    b.result should equal (Success(4))
  }

  test("With Parameter type test") {
    val w = new ExtendedWiredParam[Option] {
      def get = Some(4)
    }
    val j = new JoinParam[ExtendedWiredParam] {
      type In[+T] = Option[T]
      type Out[+T] = Try[T]

      def wired = w

      def convert[T](i: In[T]) =
        i.map(Success(_)).getOrElse(Failure(new Exception("Not found")))
    }
    val b = new ExtendedBaseParam {
      type Result[+T] = Try[T]
      def join = j
    }

    b.result should equal (Success(4))
  }
}

trait Base {

  type Result[+T]

  def join:Join{type Out[+T] = Result[T]}
}

trait Join {
  type In[+T]
  type Out[+T]
  type Wire <: Wired

  def wired:Wire{type W[+T] = In[T]}

  def apply[T](f:this.type => Out[T]) =
    f(this)

  def convert[T](i:In[T]):Out[T]
}

trait Wired {
  type W[+T]
}


trait ExtendedWired extends Wired {
  def get:W[Int]
}

trait ExtendedBase extends Base {

  def join:Join{type Out[+T] = Result[T];type Wire = ExtendedWired}

  def result = join { j =>
    j.convert(j.wired.get)
  }
}

trait BaseParam[W[T[+_]] <: WiredParam[T]]  {

  type Result[+T]

  def join:JoinParam[W]{type Out[+T] = Result[T]}
}

trait JoinParam[W[T[+_]] <: WiredParam[T]] {
  type In[+T]
  type Out[+T]

  def wired:W[In]

  def apply[T](f:this.type => Out[T]) =
    f(this)

  def convert[T](i:In[T]):Out[T]
}

trait WiredParam[W[+_]]

trait ExtendedWiredParam[W[+_]] extends WiredParam[W] {
  def get:W[Int]
}

trait ExtendedBaseParam extends BaseParam[ExtendedWiredParam] {

  def result = join { j =>
    j.convert(j.wired.get)
  }
}

trait HasType {
  type A[+T]
}

trait OptionType1 extends HasType {
  type Option[+T]
}

trait OptionType2 extends HasType {
  type Option[+T]
}


trait WithNestedHasType[W[+_]] {

  trait NestedHasType extends HasType {
    type A[+T] <: W[T]
  }
}


