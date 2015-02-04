package org.higherState.cqrs.std

import scalaz.~>
import scala.concurrent.ExecutionContext
import org.higherState.cqrs._


trait IdentityTransforms {

  implicit def DirectPipe[T[_]] = new ~>[T, T] {
    def apply[A](fa: T[A]): T[A] = fa
  }

  implicit def IdentityValidationPipe[E] = new ~>[Id, ({type V[+T] = Valid[E,T]})#V] {
    def apply[A](fa: Id[A]): Valid[E, A] =
      scalaz.Success(fa)
  }

  implicit val IdentityFuturePipe = new ~>[Id, scala.concurrent.Future] {
    def apply[T](value:Id[T]): scala.concurrent.Future[T] =
      scala.concurrent.Future.successful(value)
  }

  implicit def IdentityFutureValidPipe[E] = new ~>[Id, ({type V[+T] = FutureValid[E,T]})#V] {
    def apply[T](value: Id[T]): FutureValid[E, T] =
      scala.concurrent.Future.successful(scalaz.Success(value))
  }
}

trait ValidTransforms {

  implicit def ValidationFutureValidPipe[E] = new ~>[ ({type V[+T] = Valid[E,T]})#V,  ({type V[+T] = FutureValid[E,T]})#V] {
    def apply[T](value: Valid[E, T]): FutureValid[E, T] =
      scala.concurrent.Future.successful(value)
  }
}

trait FutureTransforms {
  import scala.concurrent.Future

  implicit def FutureFutureValidationPipe[E](implicit ec:ExecutionContext) =
    new ~>[Future, ({type V[+T] = FutureValid[E,T]})#V] {
      def apply[T](value: Future[T]): FutureValid[E,T] =
        value.map(scalaz.Success(_))
    }
}

object NaturalTransforms extends IdentityTransforms with ValidTransforms with FutureTransforms
