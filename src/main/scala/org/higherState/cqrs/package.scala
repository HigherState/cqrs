package org.higherState

import scalaz._
import scala.concurrent.Future

package object cqrs {

  type Identity[+T] = T
  //because typing traversableOnce all the time is a pain
  type Iter[+A] = TraversableOnce[A]

  type Valid[+T] = ValidationNel[ValidationFailure, T]

  type ValidationFailures = NonEmptyList[ValidationFailure]

  type FutureValid[+T] = Future[ValidationNel[ValidationFailure, T]]

}
