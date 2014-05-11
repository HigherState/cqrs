package org.higherState

import scalaz._
import scala.concurrent.Future

package object cqrs {

  type Identity[+T] = T

  type Iter[+A] = TraversableOnce[A]

  type ValidationFailures = NonEmptyList[ValidationFailure]

  type Valid[+T] = ValidationNel[ValidationFailure, T]

  type ValidUnit = ValidationNel[ValidationFailure, Unit]

  type ValidResult = ValidationNel[ValidationFailure, Any]

  type ValidIter[+T] = ValidationNel[ValidationFailure, Iter[T]]

  type ValidOption[+T] = ValidationNel[ValidationFailure, Option[T]]

  type FutureValid[+T] = Future[ValidationNel[ValidationFailure, T]]


  //Experimenting
  //Shamelessly ripped from Shapeless
  //https://github.com/milessabin/shapeless/blob/master/core/src/main/scala/shapeless/package.scala
  def unexpected : Nothing = sys.error("Unexpected invocation")
  trait =:!=[A, B]

  implicit def neq[A, B] : A =:!= B = new =:!=[A, B] {}
  implicit def neqAmbig1[A] : A =:!= A = unexpected
  implicit def neqAmbig2[A] : A =:!= A = unexpected

}
