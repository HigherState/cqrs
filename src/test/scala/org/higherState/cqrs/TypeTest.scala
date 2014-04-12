package org.higherState.cqrs

import org.scalatest.{Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures

/**
 * Created by Jamie Pullar on 12/04/2014.
 */
class TypeTest extends  FunSuite with Matchers with ScalaFutures {

  test("ActorService") {
    val t = new Option2{}
    val i = new Identity2{}
    t.result(3)
    i.result(3)
  }
}


trait Base {

  type A[+T]

  def result[T](t:T):A[T]
}

trait Identity1 extends Base {

  type A[+T] = T

  def result[T](t: T): A[T] = t
}

trait Options extends Base {

  type A[+T] = Option[T]

  def result[T](t: T): A[T] = Some(t)
}

trait WithBase {

  type R[+T]
  def base:Base{type A[+T] = R[T]}

  def apply[T](t:T) =
    base.result(t)
}

trait Base2[A[_]] {
  def result[T](t:T):A[T]
}

trait Identity2 extends Base2[Identity] {
  def result[T](t: T): T = t
}

trait Option2 extends Base2[Option] {

  def result[T](t: T): Option[T] = Some(t)
}



