package org.higherState

import scalaz._
import scala.concurrent.Future

package object cqrs {

  type Id[+T] = T
  //because typing traversableOnce all the time is a pain
  type Iter[+A] = TraversableOnce[A]

  type ValidE[+E] = Valid[E, _]

  type Valid[+E, +T] = ValidationNel[E, T]

  type EitherValid[+E, +T] = scalaz.\/[NonEmptyList[E], T]

  type FutureValid[+E, +T] = Future[ValidationNel[E, T]]

  type FutureEitherValid[+E, +T] = Future[EitherValid[E, T]]

}
