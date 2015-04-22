package org.higherState.cqrs

import scalaz.{NonEmptyList, ~>}
import scalaz.syntax.{Ops, ToMonadOps}

object ServicePipe {
  implicit def ~>[In[+_], Out[+_],T](value:In[T])(implicit nt: ~>[In,Out]):Out[T]
  = nt.apply(value)
}

object Monad extends ToMonadOps {
  def ~>[In[+_], Out[+_],T](value:In[T])(implicit nt: ~>[In,Out]):Out[T]
     = nt.apply(value)
}

trait FMonad[E, Out[+_]] extends scalaz.Monad[Out] {

  def failure(validationFailure: => E):Out[Nothing]

  def failures(validationFailures: => NonEmptyList[E]):Out[Nothing]

  def onFailure[T, S >: T](value:Out[T])(f:NonEmptyList[E] => Out[S]):Out[S]
}

object FMonad extends ToMonadOps {
  def ~>[In[+_], Out[+_],T](value:In[T])(implicit nt: ~>[In,Out]):Out[T]
    = nt.apply(value)

  def failure[E, Out[+_]](validationFailure: => E)(implicit fmonad:FMonad[E, Out]):Out[Nothing] =
    fmonad.failures(NonEmptyList(validationFailure))

  def failures[E, Out[+_]](validationFailures: => NonEmptyList[E])(implicit fmonad:FMonad[E, Out]):Out[Nothing] =
    fmonad.failures(validationFailures)

}

final class FMonadOps[E, Out[+_] ,T] private[cqrs](val self: Out[T])(implicit val F: FMonad[E, Out]) extends Ops[Out[T]] {
  def onFailure[S >: T](f:NonEmptyList[E] => Out[S]):Out[S] =
    F.onFailure[T,S](self)(f)
}

