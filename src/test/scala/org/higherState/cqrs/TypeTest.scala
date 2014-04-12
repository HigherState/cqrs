package org.higherState.cqrs

import org.scalatest.{Matchers, FunSuite}
import org.scalatest.concurrent.ScalaFutures

/**
 * Created by Jamie Pullar on 12/04/2014.
 */
class TypeTest extends  FunSuite with Matchers with ScalaFutures {

  test("ActorService") {
    val a = new WithBase {

      type R[+T] = T
      def base = new Identity{}
    }
  }
}


trait Base {

  type A[+T]

  def result[T](t:T):A[T]
}

trait Identity extends Base {

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
//
//trait Base2[A[+T]] {
//  def result[T](t:T):A[T]
//}
//
//trait Identity2 extends Base2{type A[+T] = T} {
//
//  def result[T](t: T): A[T] = t
//}

