package org.higherState

import scalaz._
import akka.actor.{ActorRef, ActorSelection}

package object cqrs {
  trait TF {
    type I[Y[+_]]
  }
  type ~>![X[+_]] = TF { type I[Y[+_]] = ~>[Y, X] }

  type EitherActor = Either[ActorRef, ActorSelection]
  type Monad[F[_]] = scalaz.Monad[F]

  val >> = AsActor
}
