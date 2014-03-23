package org.higherState

import scalaz._

package object cqrs {

  type Iter[+A] = TraversableOnce[A]

  type ValidationFailures = NonEmptyList[ValidationFailure]

  type Valid[R] = ValidationNel[ValidationFailure, R]

  type ValidUnit = ValidationNel[ValidationFailure, Unit]

  type ValidResult = ValidationNel[ValidationFailure, Any]

  type ValidIter[R] = ValidationNel[ValidationFailure, Iter[R]]

  type ValidOption[R] = ValidationNel[ValidationFailure, Option[R]]
}
