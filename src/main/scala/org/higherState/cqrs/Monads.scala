package org.higherState.cqrs

import scalaz._

import scalaz.Success

trait IdentityMonad {

  implicit val identity = new Monad[Identity] {
    def bind[A, B](fa: Identity[A])(f: (A) => Identity[B]): Identity[B] =
      f(fa)
    def point[A](a: => A): Identity[A] = a
  }
}

trait ScalazMonads {

  implicit val validation = new Monad[Valid] {
    def bind[A, B](fa: Valid[A])(f: (A) => Valid[B]): Valid[B] =
      fa.flatMap(f)
    def point[A](a: => A): Valid[A] =
      Success(a)
  }

  implicit val eitherValid = new Monad[EitherValid] {
    def bind[A, B](fa: EitherValid[A])(f: (A) => EitherValid[B]): EitherValid[B] =
      fa.flatMap(f)
    def point[A](a: => A): EitherValid[A] =
      \/-(a)
  }
}

trait FutureMonadz {
  import scalaz.concurrent.Future

  implicit val futurez = new Monad[Future] {
    def bind[A, B](fa: Future[A])(f: (A) => Future[B]): Future[B] =
      fa.flatMap(f)
    def point[A](a: => A): Future[A] =
      Future.now(a)
  }

  implicit val futurezValidation = new Monad[FutureValidz] {
    def bind[A, B](fa: FutureValidz[A])(f: (A) => FutureValidz[B]): FutureValidz[B] =
      fa.flatMap {
        case Failure(vf) =>
          Future.now(Failure(vf))
        case Success(t) =>
          f(t)
      }

    def point[A](a: => A): FutureValidz[A] =
      Future.now(Success(a))
  }

  implicit val futurezEitherValid = new Monad[FutureEitherValidz] {
    def bind[A, B](fa: FutureEitherValidz[A])(f: (A) => FutureEitherValidz[B]): FutureEitherValidz[B] =
      fa.flatMap {
        case -\/(vf) =>
          Future.now(-\/(vf))
        case \/-(t) =>
          f(t)
      }
    def point[A](a: => A): FutureEitherValidz[A] =
      Future.now(\/-(a))
  }
}

trait FutureMonads {
  import scala.concurrent.Future

  implicit val future = new Monad[Future] {
    def bind[A, B](fa: Future[A])(f: (A) => Future[B]): Future[B] =
      FlattenFuture(fa)(f)
    def point[A](a: => A): Future[A] =
      Future.successful(a)
  }

  implicit val futureValidation = new Monad[FutureValid] {
    def bind[A, B](fa: FutureValid[A])(f: (A) => FutureValid[B]): FutureValid[B] =
      FlattenFuture(fa) {
        case Failure(vf) =>
          Future.successful(Failure(vf))
        case Success(t) =>
          f(t)
      }

    def point[A](a: => A): FutureValid[A] =
      Future.successful(Success(a))
  }

  implicit val futureEitherValid = new Monad[FutureEitherValid] {
    def bind[A, B](fa: FutureEitherValid[A])(f: (A) => FutureEitherValid[B]): FutureEitherValid[B] =
      FlattenFuture(fa) {
        case -\/(vf) =>
          Future.successful(-\/(vf))
        case \/-(t) =>
          f(t)
      }
    def point[A](a: => A): FutureEitherValid[A] =
      Future.successful(\/-(a))
  }
}

object Monads extends IdentityMonad with ScalazMonads with FutureMonads with FutureMonadz

