package org.higherState.cqrs

import scalaz.{NonEmptyList, Monad}

//Bindings, doesnt seem the best name
trait MonadBind[Out[+_]] extends Monad[Out]{

  protected def monad:Monad[Out]

  def bind[A, B](fa: Out[A])(f: (A) => Out[B]): Out[B] =
    monad.bind(fa)(f)

  def point[A](a: => A): Out[A] =
    monad.point(a)

}

trait FMonadBind[E, Out[+_]] extends FMonad[E, Out] with MonadBind[Out]  {

  protected def monad:FMonad[E, Out]

  def failure(validationFailure: => E): Out[Nothing] =
    monad.failure(validationFailure)

  def failures(validationFailures: => NonEmptyList[E]): Out[Nothing] =
    monad.failures(validationFailures)
}

abstract class MonadBound[Out[+_]:Monad] extends MonadBind[Out]{
  protected def monad:Monad[Out] = implicitly[Monad[Out]]
}


