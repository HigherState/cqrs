package org.higherState.cqrs

import scalaz._

object ServicePipe {
  implicit def ~>[In[+_], Out[+_],T](value:In[T])(implicit nt: ~>[In,Out]):Out[T]
  = nt.apply(value)
}

object Monad {

  implicit class PipeMonad[Out[+_], In[+_], A](in: In[A])(implicit monad: Monad[Out], pipe:In ~> Out) {
    def flatMap[T](f:A => Out[T]):Out[T] =
      monad.bind(pipe(in))(f)
    def map[T](f:A => T):Out[T] =
      monad.map(pipe(in))(f)
  }
}

trait FMonad[E, Out[+_]] extends Monad[Out] {

  def failure(validationFailure: => E):Out[Nothing]

  def failures(validationFailures: => NonEmptyList[E]):Out[Nothing]

  def onFailure[T, S >: T](value:Out[T])(f:NonEmptyList[E] => Out[S]):Out[S]
}

object FMonad {
  def ~>[In[+_], Out[+_],T](value:In[T])(implicit nt: ~>[In,Out]):Out[T]
    = nt.apply(value)


  def failure[E, Out[+_]](validationFailure: => E)(implicit fmonad:FMonad[E, Out]):Out[Nothing] =
    fmonad.failures(NonEmptyList(validationFailure))

  def failures[E, Out[+_]](validationFailures: => NonEmptyList[E])(implicit fmonad:FMonad[E, Out]):Out[Nothing] =
    fmonad.failures(validationFailures)

  implicit class PipeMonad[Out[+_], In[+_], A](in: In[A])(implicit monad: Monad[Out], pipe:In ~> Out) {
    def flatMap[T](f:A => Out[T]):Out[T] =
      monad.bind(pipe(in))(f)
    def map[T](f:A => T):Out[T] =
      monad.map(pipe(in))(f)
  }

}
