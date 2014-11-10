package org.higherState.cqrs

import scalaz._
import scalaz.Success

trait Directives[Out[+_]] {

  import scalaz.std.list._

  implicit protected def fm:Monad[Out]

  def complete:Out[Unit] =
    fm.point(Unit)

  def unit[T](value: => T):Out[T] =
    fm.point(value)

  def flatMap[T,U](output:Out[T])(f:T => Out[U]):Out[U] =
    fm.bind(output)(f)

  def map[T,U](output:Out[T])(f:T => U):Out[U] =
    fm.map(output)(f)

  def sequence[T,U](r1:Out[T], r2:Out[T], r:Out[T]*)(f:Seq[T] => Out[U]):Out[U] =
    sequence(r1 :: r2 :: r.toList)(f)

  def sequence[T,U, G[_]](r: => G[Out[T]])(f:G[T] => Out[U])(implicit ev:Traverse[G]):Out[U] =
    fm.bind(fm.sequence(r))(f)

  def foreach(r1:Out[Unit], r2:Out[Unit], r:Out[Unit]*):Out[Unit] =
    sequence(r1 :: r2 :: r.toList)(_ => complete)

  def foreach[G[_]](f: => G[Out[Unit]])(implicit ev:Traverse[G]):Out[Unit] =
    sequence(f)(_ => complete)

}

trait FailureDirectives[Out[+_]] extends Directives[Out] {

  def failure[T](failure: => ValidationFailure):Out[T] = ???

  def failures[T](failed: => NonEmptyList[ValidationFailure]):Out[T] = ???

  def failures[T](head:ValidationFailure, tail:Seq[ValidationFailure]):Out[T]
  = ???

  def onValid[T, U](v:Valid[T])(f:T => Out[U]):Out[U] =
    v match {
      case Success(t) =>
        f(t)
      case Failure(vf) =>
        failures[U](vf)
    }

  def onFailure[T](value:Out[T])(f:NonEmptyList[ValidationFailure] => Out[T]):Out[T]
    = ???

  def validationSequence[T,U](v:Iter[Out[T]])(f:Iter[Valid[T]] => Out[U]):Out[U]
    = ???

}
