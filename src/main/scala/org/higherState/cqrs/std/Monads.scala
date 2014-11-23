package org.higherState.cqrs.std

import scala.concurrent.ExecutionContext

object IdMonad {
  implicit val idMonad = scalaz.Id.id
}

object FutureMonad {
  implicit def futureMonad(implicit ec:ExecutionContext) =
    scalaz.std.scalaFuture.futureInstance(ec)
}
