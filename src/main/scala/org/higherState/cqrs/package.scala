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
  class =!=[A, B] private() extends NotNull

  object =!= {
    implicit def notMeantToBeCalled1[A, B >: A, C >: B <: A]: =!=[B, A] = error("should not be called")

    implicit def notMeantToBeCalled2[A, B >: A, C >: B <: A]: =!=[B, A] = error("should not be called")

    implicit def unambigouslyDifferent[A, B](implicit same: A =:= B = null): =!=[A, B] =
      if (same != null) error("should not be called explicitly with the same type")
      else new =!=
  }

  trait =:!=[A, B]

  implicit def neq[A, B]: A =:!= B = new =:!=[A, B] {}

  implicit def neqAmbig1[A]: A =:!= A = error("should not be called")

  implicit def neqAmbig2[A]: A =:!= A = error("should not be called")
}
