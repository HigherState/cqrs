package org.higherState

import scalaz._
import scala.concurrent.Future

package object cqrs {

  type Identity[+T] = T
  //because typing traversableOnce all the time is a pain
  type Iter[+A] = TraversableOnce[A]

  type ValidationFailures = NonEmptyList[ValidationFailure]

  type Valid[+T] = ValidationNel[ValidationFailure, T]

  type ValidUnit = ValidationNel[ValidationFailure, Unit]

  type ValidResult = ValidationNel[ValidationFailure, Any]

  type ValidIter[+T] = ValidationNel[ValidationFailure, Iter[T]]

  type ValidOption[+T] = ValidationNel[ValidationFailure, Option[T]]

  type FutureValid[+T] = Future[ValidationNel[ValidationFailure, T]]


  implicit class TraverableValid[T](val self:TraversableOnce[Valid[T]]) extends AnyVal {

    def sequence =
      SequenceHelper(self)
  }

}
