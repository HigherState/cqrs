package org.higherState

import scalaz._
import scala.concurrent.Future

package object cqrs {

  type Identity[+T] = T
  //because typing traversableOnce all the time is a pain
  type Iter[+A] = TraversableOnce[A]

  type Valid[+T] = ValidationNel[ValidationFailure, T]

  type EitherValid[+T] = scalaz.\/[ValidationFailures, T]

  type ValidationFailures = NonEmptyList[ValidationFailure]

  type FutureValid[+T] = Future[ValidationNel[ValidationFailure, T]]

  type FutureEitherValid[+T] = Future[EitherValid[T]]

  type FutureValidz[+T] = scalaz.concurrent.Future[ValidationNel[ValidationFailure, T]]

  type FutureEitherValidz[+T] = scalaz.concurrent.Future[EitherValid[T]]

}
