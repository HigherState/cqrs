package org.higherState.cqrs.pipes

import org.higherState.cqrs.{Identity, ValidationFailure, Service}
import org.higherState.cqrs.directives._
import scala.concurrent.{Future, ExecutionContext}
import scalaz.{Success, Failure, ValidationNel}

trait Pipe extends Directives {

  type In[+T]

  def success[T](value: => In[T]):Out[T] =
    onSuccess[T,T](value)(t => result(t))

  def onSuccessComplete[T](value: => In[T]):Out[Unit] =
    onSuccess[T, Unit](value)(t => complete)

  def onSuccess[S, T](value:In[S])(f : (S) => Out[T]): Out[T]

  def foreach(func: => TraversableOnce[In[Unit]]):Out[Unit]
}

trait ServicePipe[S[_[_]]] extends Pipe {
  def service:S[In]

  def apply[T](f:this.type => Out[T]) =
    f(this)
}

trait IdentityPipe extends Pipe with IdentityDirectives {

  type In[+T] = Identity[T]

  def onSuccess[S, T](value:Out[S])(f : (S) => In[T]) =
    f(value)

  def foreach(func: => TraversableOnce[Out[Unit]]):In[Unit] =
    complete
}

trait FuturePipe extends Pipe with FutureDirectives {

  implicit def executionContext:ExecutionContext

  type In[+T] = Future[T]

  def onSuccess[S, T](value:In[S])(f : (S) => Out[T]): Out[T] =
    value.flatMap(f)

  def foreach(func: => TraversableOnce[In[Unit]]):Out[Unit] =
    Future.sequence(func).map(t => Unit)
}

trait ValidationPipe extends Pipe with FailureDirectives {

  type In[+T] = ValidationNel[ValidationFailure, T]

  def foreach(func: => TraversableOnce[In[Unit]]):Out[Unit] =
    func.toIterator.collect {
      case Failure(f) => f
    }.reduceOption(_.append(_))
      .map(f => failures[Unit](f))
      .getOrElse(complete)


  def onSuccess[S, T](value:In[S])(f : (S) => Out[T]): Out[T] =
    value match {
      case Failure(vf) =>
        failures[T](vf)
      case Success(s) =>
        f(s)
    }
}

trait FutureValidationPipe extends Pipe with FutureValidationDirectives {

  type In[+T] = Future[ValidationNel[ValidationFailure, T]]

  def foreach(func:TraversableOnce[In[Unit]]):Out[Unit] =
    Future.sequence(func).map(_.foldLeft[ValidationNel[ValidationFailure, Unit]](Success(Unit)) {
      case (v, Success(_)) =>
        v
      case (Failure(v), Failure(vf)) =>
        Failure(v.append(vf))
      case (_,c) =>
        c
    })

  def onSuccess[S, T](value:In[S])(f : (S) => Out[T]): Out[T] =
    value.flatMap{
      case Success(s) =>
        f(s)
      case Failure(vf) =>
        failures(vf)
    }
}

