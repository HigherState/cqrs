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
}
