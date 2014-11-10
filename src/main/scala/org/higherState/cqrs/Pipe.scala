package org.higherState.cqrs

import scalaz.concurrent.Future

trait Pipe[In[+_], Out[+_]] {

  def apply[T](f: => In[T]):Out[T]
}

trait IdentityPipes {

  implicit val IdentityIdentityPipe = new Pipe[Identity, Identity] {
    def apply[T](f: => Identity[T]): Identity[T] = f
  }

  implicit val IdentityValidationPipe = new Pipe[Identity, Valid] {
    def apply[T](f: => Identity[T]): Valid[T] =
      scalaz.Success(f)
  }

  implicit val IdentityFuturePipe = new Pipe[Identity, scala.concurrent.Future] {
    def apply[T](value: => Identity[T]): scala.concurrent.Future[T] =
      scala.concurrent.Future.successful(value)
  }

  implicit val IdentityFutureValidPipe = new Pipe[Identity, FutureValid] {
    def apply[T](value: => Identity[T]): FutureValid[T] =
      scala.concurrent.Future.successful(scalaz.Success(value))
  }

  implicit val IdentityFuturezPipe = new Pipe[Identity, scalaz.concurrent.Future] {
    def apply[T](value: => Identity[T]): Future[T] =
      scalaz.concurrent.Future.now(value)
  }

  implicit val IdentityFutureValidzPipe = new Pipe[Identity, FutureValidz] {
    def apply[T](value: => Identity[T]): FutureValidz[T] =
      scalaz.concurrent.Future.now(scalaz.Success(value))
  }
}

trait ValidPipes {

  implicit val ValidationValidationPipe = new Pipe[Valid, Valid] {
    def apply[T](value: => Valid[T]): Valid[T] =
      value
  }

  implicit val ValidationFutureValidPipe = new Pipe[Valid, FutureValid] {
    def apply[T](value: => Valid[T]): FutureValid[T] =
      scala.concurrent.Future.successful(value)
  }

  implicit val ValidationFutureValidzPipe = new Pipe[Valid, FutureValidz] {
    def apply[T](value: => Valid[T]): FutureValidz[T] =
      scalaz.concurrent.Future.now(value)
  }
}

trait FuturePipes {
  import scala.concurrent.Future

  implicit val FutureFuturePipe = new Pipe[Future, Future] {
    def apply[T](value: => Future[T]): Future[T] =
      value
  }

  implicit val FutureFutureValidationPipe = new Pipe[Future, FutureValid] {
    def apply[T](value: => Future[T]): FutureValid[T] =
      MapFuture(value)(scalaz.Success(_))
  }
}

trait FuturezPipes {
  import scalaz.concurrent.Future

  implicit val FuturezFuturezPipe = new Pipe[Future, Future] {
    def apply[T](value: => Future[T]): Future[T] =
      value
  }

  implicit val FuturezFutureValidzPipe = new Pipe[Future, FutureValidz] {
    def apply[T](value: => Future[T]): FutureValidz[T] =
      value.map(scalaz.Success(_))
  }
}

trait FutureValidPipes {

  implicit val FutureValidationFutureValidationPipe = new Pipe[FutureValid, FutureValid] {
    def apply[T](value: => FutureValid[T]): FutureValid[T] =
      value
  }
}

trait FutureValidzPipes {

  implicit val FutureValidationFutureValidationPipe = new Pipe[FutureValidz, FutureValidz] {
    def apply[T](value: => FutureValidz[T]): FutureValidz[T] =
      value
  }
}

object Pipes extends IdentityPipes with ValidPipes with FuturePipes with FutureValidPipes

object Pipez extends IdentityPipes with ValidPipes with FuturezPipes with FutureValidzPipes
