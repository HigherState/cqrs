package org.higherState.cqrs

import scalaz._
import scala.concurrent.Future

/**
 * Created by jamie.pullar on 04/02/2015.
 */
package object std {

  type Id[+T] = T

  type ValidE[+E] = Valid[E, _]

  type Valid[+E, +T] = ValidationNel[E, T]

  type EitherValid[+E, +T] = scalaz.\/[NonEmptyList[E], T]

  type FutureValid[+E, +T] = Future[ValidationNel[E, T]]

  type FutureEitherValid[+E, +T] = Future[EitherValid[E, T]]

}
