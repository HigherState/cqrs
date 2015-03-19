package org.higherState.cqrs.std

import scalaz._

import scala.concurrent.Future
import org.higherState.cqrs._
import scalaz.-\/
import scalaz.Success
import scalaz.\/-
import scalaz.Failure

object VFMonad extends VFMonadImplicits

object DFMonad extends DFMonadImplicits

trait VFMonadImplicits {

  implicit def vfMonad[E] = new FMonad[E, ({type V[+T] = Valid[E,T]})#V] {
    def bind[A, B](fa: Valid[E, A])(f: (A) => Valid[E, B]): Valid[E, B] =
      fa.flatMap(f)

    def point[A](a: => A): Valid[E, A] =
      Success(a)

    def failures(validationFailures: => NonEmptyList[E]): Valid[E, Nothing] =
      Failure(validationFailures)

    def failure(validationFailure: => E): Valid[E, Nothing] =
      Failure(NonEmptyList(validationFailure))

    def onFailure[T, S >: T](value: Valid[E, T])(f: (NonEmptyList[E]) => Valid[E, S]):Valid[E, S] =
      value match {
        case Failure(n) => f(n)
        case e => e
      }
  }
  implicit def futureVFMonad[E](implicit monad:Monad[Future]):FMonad[E, ({type V[+T] = FutureValid[E,T]})#V] =
    new FMonad[E, ({type V[+T] = FutureValid[E,T]})#V] {
      def bind[A, B](fa: FutureValid[E, A])(f: (A) => FutureValid[E, B]): FutureValid[E, B] =
        monad.bind(fa) {
          case Failure(vf) =>
            monad.point(vfMonad.failures(vf))
          case Success(s) => f(s)
        }

      def point[A](a: => A): FutureValid[E, A] =
        monad.point(vfMonad.point(a))

      def failure(validationFailure: => E): FutureValid[E, Nothing] =
        monad.point(vfMonad.failure(validationFailure))

      def failures(validationFailures: => NonEmptyList[E]):FutureValid[E, Nothing] =
        monad.point(vfMonad.failures(validationFailures))

      def onFailure[T, S >: T](value: FutureValid[E, T])(f: (NonEmptyList[E]) => FutureValid[E, S]):FutureValid[E, S] =
        monad.bind(value) {
          case Failure(vf) =>
            f(vf)
          case e => monad.point(e)
        }
    }
}
trait DFMonadImplicits {
  implicit def dfMonad[E] = new FMonad[E, ({type V[+T] = EitherValid[E,T]})#V] {
    def bind[A, B](fa: EitherValid[E, A])(f: (A) => EitherValid[E, B]): EitherValid[E, B] =
      fa.flatMap(f)
    def point[A](a: => A): EitherValid[E, A] =
      \/-(a)

    def failure(validationFailure: => E): EitherValid[E, Nothing] =
      -\/(NonEmptyList(validationFailure))

    def failures(validationFailures: => NonEmptyList[E]): EitherValid[E, Nothing] =
      -\/(validationFailures)

    def onFailure[T, S >: T](value: EitherValid[E, T])(f: (NonEmptyList[E]) => EitherValid[E, S]):EitherValid[E, S] =
      value match {
        case -\/(n) => f(n)
        case e => e
      }
  }
  implicit def futureDFMonad[E](implicit monad:Monad[Future]):FMonad[E, ({type V[+T] = FutureEitherValid[E,T]})#V] =
    new FMonad[E, ({type V[+T] = FutureEitherValid[E,T]})#V] {
      def bind[A, B](fa: FutureEitherValid[E, A])(f: (A) => FutureEitherValid[E, B]): FutureEitherValid[E, B] =
        monad.bind(fa) {
          case -\/(vf) =>
            monad.point(dfMonad.failures(vf))
          case \/-(s) => f(s)
        }

      def point[A](a: => A): FutureEitherValid[E, A] =
        monad.point(dfMonad.point(a))

      def failure(validationFailure: => E): FutureEitherValid[E, Nothing] =
        monad.point(dfMonad.failure(validationFailure))

      def failures(validationFailures: => NonEmptyList[E]):FutureEitherValid[E, Nothing] =
        monad.point(dfMonad.failures(validationFailures))

      def onFailure[T, S >: T](value: FutureEitherValid[E, T])(f: (NonEmptyList[E]) => FutureEitherValid[E, S]):FutureEitherValid[E, S] =
        monad.bind(value) {
          case -\/(vf) =>
            f(vf)
          case e => monad.point(e)
        }
    }

}

