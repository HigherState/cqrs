package org.higherState.cqrs

import scalaz._
import scala.concurrent.Future

package object std {

  type Id[+T] = T

  type ValidE[+E] = Valid[E, _]

  type Valid[+E, +T] = ValidationNel[E, T]

  type ReaderValid[F, +E, +T] = Reader[F, Valid[E, T]]

  type EitherValid[+E, +T] = scalaz.\/[NonEmptyList[E], T]

  type FutureValid[+E, +T] = Future[ValidationNel[E, T]]

  type FutureReader[F, +T] = Future[Reader[F, T]]

  type FutureReaderValid[F, +E, +T] = Future[ReaderValid[F, E, T]]

  type FutureEitherValid[+E, +T] = Future[EitherValid[E, T]]

}
