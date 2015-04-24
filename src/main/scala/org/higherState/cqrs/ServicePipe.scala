package org.higherState.cqrs

import scalaz._
import scalaz.syntax.ToMonadOps


trait PipeOps {
  implicit def ~>[In[+_], Out[+_],T](value:In[T])(implicit nt: ~>[In,Out]):Out[T]
    = nt.apply(value)
}

object ServicePipe extends PipeOps

trait PipeMonad {
  implicit class PipeMonad[Out[+_], In[+_], A](in: In[A])(implicit monad: Monad[Out], pipe:In ~> Out) {
    def flatMap[T](f:A => Out[T]):Out[T] =
      monad.bind(pipe(in))(f)
    def map[T](f:A => T):Out[T] =
      monad.map(pipe(in))(f)
  }
}

trait MonadOps {
  import scalaz.Scalaz._

  def point[Out[+_], T](t: => T)(implicit monad:Monad[Out]):Out[T] =
    monad.point(t)

  def sequence[Out[+_], T](l:List[Out[T]])(implicit monad:Monad[Out]):Out[List[T]] =
    monad.sequence(l)
}

object Monad extends PipeMonad with PipeOps with MonadOps with ToMonadOps

trait FMonad[E, Out[+_]] extends Monad[Out] {

  def failure(validationFailure: => E):Out[Nothing]

  def failures(validationFailures: => NonEmptyList[E]):Out[Nothing]

  def onFailure[T, S >: T](value:Out[T])(f:NonEmptyList[E] => Out[S]):Out[S]
}

trait PipeFMonad {
  implicit class PipeMonad[Out[+_], In[+_], E, A](in: In[A])(implicit monad: FMonad[E, Out], pipe:In ~> Out) {
    def flatMap[T](f:A => Out[T]):Out[T] =
      monad.bind(pipe(in))(f)
    def map[T](f:A => T):Out[T] =
      monad.map(pipe(in))(f)
    def onFailure[T >: A](f:NonEmptyList[E] => Out[T]):Out[T] =
      monad.onFailure[A, T](pipe(in))(f)
  }
  implicit class FailureMonad[Out[+_], E, A](out:Out[A])(implicit monad: FMonad[E, Out]) {
    def onFailure[T >: A](f:NonEmptyList[E] => Out[T]):Out[T] =
      monad.onFailure[A, T](out)(f)
  }
}

trait FMonadOps extends MonadOps {
  def failure[E, Out[+_]](validationFailure: => E)(implicit fmonad:FMonad[E, Out]):Out[Nothing] =
    fmonad.failures(NonEmptyList(validationFailure))

  def failures[E, Out[+_]](validationFailures: => NonEmptyList[E])(implicit fmonad:FMonad[E, Out]):Out[Nothing] =
    fmonad.failures(validationFailures)
}

object FMonad extends PipeFMonad with PipeOps with FMonadOps with ToMonadOps

object Scalaz extends
  StateFunctions        // Functions related to the state monad
  with scalaz.syntax.ToTypeClassOps    // syntax associated with type classes
  with scalaz.syntax.ToDataOps         // syntax associated with Scalaz data structures
  //with scalaz.std.AllInstances         // BREAKS Monads
  with scalaz.std.AllFunctions         // Functions related to standard library types
  with scalaz.syntax.std.ToAllStdOps   // syntax associated with standard library types
  with scalaz.IdInstances              // Identity type and instances

