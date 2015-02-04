package org.higherState.cqrs.std

import scalaz._

import scala.concurrent.Future
import org.higherState.cqrs._
import scalaz.-\/
import scalaz.Success
import scalaz.\/-
import scalaz.Failure

object ValidationValidator extends ValidationValidator

object EitherValidValidator extends EitherValidValidator

trait ValidationValidator {

  implicit def validationValidator[E] = new Validator[E, ({type V[+T] = Valid[E,T]})#V] {
    def bind[A, B](fa: Valid[E, A])(f: (A) => Valid[E, B]): Valid[E, B] =
      fa.flatMap(f)

    def point[A](a: => A): Valid[E, A] =
      Success(a)

    def failures(validationFailures: => NonEmptyList[E]): Valid[E, Nothing] =
      Failure(validationFailures)

    def failure(validationFailure: => E): Valid[E, Nothing] =
      Failure(NonEmptyList(validationFailure))
  }
  implicit def futureValidationValidator[E](implicit monad:Monad[Future]):Validator[E, ({type V[+T] = FutureValid[E,T]})#V] =
    new Validator[E, ({type V[+T] = FutureValid[E,T]})#V] {
      def bind[A, B](fa: FutureValid[E, A])(f: (A) => FutureValid[E, B]): FutureValid[E, B] =
        monad.bind(fa) {
          case Failure(vf) =>
            monad.point(validationValidator.failures(vf))
          case Success(s) => f(s)
        }

      def point[A](a: => A): FutureValid[E, A] =
        monad.point(validationValidator.point(a))

      def failure(validationFailure: => E): FutureValid[E, Nothing] =
        monad.point(validationValidator.failure(validationFailure))

      def failures(validationFailures: => NonEmptyList[E]):FutureValid[E, Nothing] =
        monad.point(validationValidator.failures(validationFailures))
    }
}
trait EitherValidValidator {
  implicit def eitherValidator[E] = new Validator[E, ({type V[+T] = EitherValid[E,T]})#V] {
    def bind[A, B](fa: EitherValid[E, A])(f: (A) => EitherValid[E, B]): EitherValid[E, B] =
      fa.flatMap(f)
    def point[A](a: => A): EitherValid[E, A] =
      \/-(a)

    def failure(validationFailure: => E): EitherValid[E, Nothing] =
      -\/(NonEmptyList(validationFailure))

    def failures(validationFailures: => NonEmptyList[E]): EitherValid[E, Nothing] =
      -\/(validationFailures)
  }
  implicit def futureEitherValidator[E](implicit monad:Monad[Future]):Validator[E, ({type V[+T] = FutureEitherValid[E,T]})#V] =
    new Validator[E, ({type V[+T] = FutureEitherValid[E,T]})#V] {
      def bind[A, B](fa: FutureEitherValid[E, A])(f: (A) => FutureEitherValid[E, B]): FutureEitherValid[E, B] =
        monad.bind(fa) {
          case -\/(vf) =>
            monad.point(eitherValidator.failures(vf))
          case \/-(s) => f(s)
        }

      def point[A](a: => A): FutureEitherValid[E, A] =
        monad.point(eitherValidator.point(a))

      def failure(validationFailure: => E): FutureEitherValid[E, Nothing] =
        monad.point(eitherValidator.failure(validationFailure))

      def failures(validationFailures: => NonEmptyList[E]):FutureEitherValid[E, Nothing] =
        monad.point(eitherValidator.failures(validationFailures))
    }

}

