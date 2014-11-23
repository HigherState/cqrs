package org.higherState.cqrs.std

import scalaz.~>
import scala.concurrent.ExecutionContext
import org.higherState.cqrs._

trait IdentityTransforms {

  implicit val IdentityIdentityPipe = new ~>[Id, Id] {
    def apply[A](fa: Id[A]): Id[A] = fa
  }

  implicit val IdentityValidationPipe = new ~>[Id, Valid] {
    def apply[A](fa: Id[A]): Valid[A] =
      scalaz.Success(fa)
  }

  implicit val IdentityFuturePipe = new ~>[Id, scala.concurrent.Future] {
    def apply[T](value:Id[T]): scala.concurrent.Future[T] =
      scala.concurrent.Future.successful(value)
  }

  implicit val IdentityFutureValidPipe = new ~>[Id, FutureValid] {
    def apply[T](value: Id[T]): FutureValid[T] =
      scala.concurrent.Future.successful(scalaz.Success(value))
  }
}

trait ValidTransforms {

  implicit val ValidationValidationPipe = new ~>[Valid, Valid] {
    def apply[T](value: Valid[T]): Valid[T] =
      value
  }

  implicit val ValidationFutureValidPipe = new ~>[Valid, FutureValid] {
    def apply[T](value: Valid[T]): FutureValid[T] =
      scala.concurrent.Future.successful(value)
  }
}

trait FutureTransforms {
  import scala.concurrent.Future

  implicit val FutureFuturePipe = new ~>[Future, Future] {
    def apply[T](value: Future[T]): Future[T] =
      value
  }

  implicit def FutureFutureValidationPipe(implicit ec:ExecutionContext) =
    new ~>[Future, FutureValid] {
      def apply[T](value: Future[T]): FutureValid[T] =
        value.map(scalaz.Success(_))
    }
}

trait FutureValidTransforms {

  implicit val FutureValidationFutureValidationPipe = new ~>[FutureValid, FutureValid] {
    def apply[T](value:FutureValid[T]): FutureValid[T] =
      value
  }
}

object NaturalTransforms extends IdentityTransforms with ValidTransforms with FutureTransforms with FutureValidTransforms
