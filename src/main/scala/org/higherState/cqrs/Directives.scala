package org.higherState.cqrs

import scalaz._
import scalaz.Success

trait Directives[Out[+_]] {

  def complete:Out[Unit]

  def unit[T](value: => T):Out[T]

  def flatMap[T,U](output:Out[T])(f:T => Out[U]):Out[U]

  def map[T,U](output:Out[T])(f:T => U):Out[U]

  def sequence[T,U](r1:Out[T], r2:Out[T], r:Out[T]*)(f:Seq[T] => Out[U]):Out[U]

  def sequence[T,U, G[_]](r: => G[Out[T]])(f:G[T] => Out[U])(implicit ev:Traverse[G]):Out[U]

  def foreach(r1:Out[Unit], r2:Out[Unit], r:Out[Unit]*):Out[Unit]

  def foreach[G[_]](f: => G[Out[Unit]])(implicit ev:Traverse[G]):Out[Unit]

}

trait FailureDirectives[Out[+_]] extends Directives[Out] {

  def failure[T](failure: => ValidationFailure):Out[T]

  def failure[T](failed: => NonEmptyList[ValidationFailure]):Out[T]

}

trait MonadicDirectives[Out[+_]] extends Directives[Out]{

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

trait MonadicFailureDirectives[Out[+_]] extends MonadicDirectives[Out] with FailureDirectives[Out] {

  implicit protected def fm:Monad[Out] with Failure[Out]

  def failure[T](failure: => ValidationFailure): Out[T] =
    fm.failed(failure)

  def failure[T](failures: => NonEmptyList[ValidationFailure]): Out[T] =
    fm.failed(failures)
}


