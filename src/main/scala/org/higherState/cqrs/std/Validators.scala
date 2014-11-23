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

  implicit val validationValidator = new Validator[Valid] {
    def bind[A, B](fa: Valid[A])(f: (A) => Valid[B]): Valid[B] =
      fa.flatMap(f)

    def point[A](a: => A): Valid[A] =
      Success(a)

    def failures(validationFailures: => NonEmptyList[ValidationFailure]): Valid[Nothing] =
      Failure(validationFailures)

    def failure(validationFailure: => ValidationFailure): Valid[Nothing] =
      Failure(NonEmptyList(validationFailure))
  }
  implicit def futureValidationValidator(implicit monad:Monad[Future]):Validator[FutureValid] =
    new Validator[FutureValid] {
      def bind[A, B](fa: FutureValid[A])(f: (A) => FutureValid[B]): FutureValid[B] =
        monad.bind(fa) {
          case Failure(vf) =>
            monad.point(validationValidator.failures(vf))
          case Success(s) => f(s)
        }

      def point[A](a: => A): FutureValid[A] =
        monad.point(validationValidator.point(a))

      def failure(validationFailure: => ValidationFailure): FutureValid[Nothing] =
        monad.point(validationValidator.failure(validationFailure))

      def failures(validationFailures: => NonEmptyList[ValidationFailure]):FutureValid[Nothing] =
        monad.point(validationValidator.failures(validationFailures))
    }
}
trait EitherValidValidator {
  implicit val eitherValidator = new Validator[EitherValid] {
    def bind[A, B](fa: EitherValid[A])(f: (A) => EitherValid[B]): EitherValid[B] =
      fa.flatMap(f)
    def point[A](a: => A): EitherValid[A] =
      \/-(a)

    def failure(validationFailure: => ValidationFailure): EitherValid[Nothing] =
      -\/(NonEmptyList(validationFailure))

    def failures(validationFailures: => NonEmptyList[ValidationFailure]): EitherValid[Nothing] =
      -\/(validationFailures)
  }
  implicit def futureEitherValidator(implicit monad:Monad[Future]):Validator[FutureEitherValid] =
    new Validator[FutureEitherValid] {
      def bind[A, B](fa: FutureEitherValid[A])(f: (A) => FutureEitherValid[B]): FutureEitherValid[B] =
        monad.bind(fa) {
          case -\/(vf) =>
            monad.point(eitherValidator.failures(vf))
          case \/-(s) => f(s)
        }

      def point[A](a: => A): FutureEitherValid[A] =
        monad.point(eitherValidator.point(a))

      def failure(validationFailure: => ValidationFailure): FutureEitherValid[Nothing] =
        monad.point(eitherValidator.failure(validationFailure))

      def failures(validationFailures: => NonEmptyList[ValidationFailure]):FutureEitherValid[Nothing] =
        monad.point(eitherValidator.failures(validationFailures))
    }

}

