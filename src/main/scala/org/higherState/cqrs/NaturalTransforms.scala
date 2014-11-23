package org.higherState.cqrs

import scalaz.concurrent.Future
import scalaz.~>
import scala.concurrent.ExecutionContext

trait IdentityTransforms {

  implicit val IdentityIdentityPipe = new ~>[Identity, Identity] {
    def apply[A](fa: Identity[A]): Identity[A] = fa
  }

  implicit val IdentityValidationPipe = new ~>[Identity, Valid] {
    def apply[A](fa: Identity[A]): Valid[A] =
      scalaz.Success(fa)
  }

  implicit val IdentityFuturePipe = new ~>[Identity, scala.concurrent.Future] {
    def apply[T](value:Identity[T]): scala.concurrent.Future[T] =
      scala.concurrent.Future.successful(value)
  }

  implicit val IdentityFutureValidPipe = new ~>[Identity, FutureValid] {
    def apply[T](value: Identity[T]): FutureValid[T] =
      scala.concurrent.Future.successful(scalaz.Success(value))
  }

  implicit val IdentityFuturezPipe = new ~>[Identity, scalaz.concurrent.Future] {
    def apply[T](value: Identity[T]): Future[T] =
      scalaz.concurrent.Future.now(value)
  }

  implicit val IdentityFutureValidzPipe = new ~>[Identity, FutureValidz] {
    def apply[T](value: Identity[T]): FutureValidz[T] =
      scalaz.concurrent.Future.now(scalaz.Success(value))
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

  implicit val ValidationFutureValidzPipe = new ~>[Valid, FutureValidz] {
    def apply[T](value: Valid[T]): FutureValidz[T] =
      scalaz.concurrent.Future.now(value)
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

trait FuturezTransforms {
  import scalaz.concurrent.Future

  implicit val FuturezFuturezPipe = new ~>[Future, Future] {
    def apply[T](value:Future[T]): Future[T] =
      value
  }

  implicit val FuturezFutureValidzPipe = new ~>[Future, FutureValidz] {
    def apply[T](value:Future[T]): FutureValidz[T] =
      value.map(scalaz.Success(_))
  }
}

trait FutureValidTransforms {

  implicit val FutureValidationFutureValidationPipe = new ~>[FutureValid, FutureValid] {
    def apply[T](value:FutureValid[T]): FutureValid[T] =
      value
  }
}

trait FutureValidzTransforms {

  implicit val FutureValidationFutureValidationPipe = new ~>[FutureValidz, FutureValidz] {
    def apply[T](value:FutureValidz[T]): FutureValidz[T] =
      value
  }
}

object NaturalTransforms extends IdentityTransforms with ValidTransforms with FutureTransforms with FutureValidTransforms

object NaturalTransformz extends IdentityTransforms with ValidTransforms with FuturezTransforms with FutureValidzTransforms
