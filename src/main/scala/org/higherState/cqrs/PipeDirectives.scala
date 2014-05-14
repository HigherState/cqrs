package org.higherState.cqrs

import scala.concurrent.{Future, ExecutionContext}
import scalaz.ValidationNel
import scalaz.Success
import scalaz.Failure

trait PipeDirectives extends Directives with Input {

  def success[T](value: => In[T]):Out[T] =
    onSuccess[T,T](value)(t => result(t))

  def onSuccessComplete[T](value: => In[T]):Out[Unit] =
    onSuccess[T, Unit](value)(t => complete)

  def onSuccess[S, T](value: => In[S])(f : (S) => Out[T]): Out[T]

  def foreach(func: => TraversableOnce[In[Unit]]):Out[Unit]
}

trait ServicePipeDirectives[S <: Service] extends PipeDirectives {
  def service:S{type Out[+T] = In[T]}
}


/*Input fixed*/
trait IdentityPipeDirectives extends PipeDirectives with Input.Identity {

  def onSuccess[S, T](value: => In[S])(f : (S) => Out[T]):Out[T] =
    f(value)

  def foreach(func: => TraversableOnce[In[Unit]]):Out[Unit] =
    complete
}

trait ValidationPipeDirectives extends PipeDirectives with FailureDirectives with Input.Valid {

  def foreach(func: => TraversableOnce[In[Unit]]):Out[Unit] =
    func.toIterator.collect {
      case Failure(f) => f
    }.reduceOption(_.append(_))
      .fold(complete)(f => failures[Unit](f))


  def onSuccess[S, T](value:In[S])(f : (S) => Out[T]): Out[T] =
    value match {
      case Failure(vf) =>
        failures[T](vf)
      case Success(s) =>
        f(s)
    }
}


/*Input and output fixed*/
trait FuturePipeDirectives extends PipeDirectives with FutureDirectives with Input.Future {

  implicit def executionContext:ExecutionContext

  def onSuccess[S, T](value: => In[S])(f : (S) => Out[T]): Out[T] =
    value.flatMap(f)

  def foreach(func: => TraversableOnce[In[Unit]]):Out[Unit] =
    Future.sequence(func).map(t => Unit)
}

trait FutureValidationPipeDirectives extends PipeDirectives with FutureValidationDirectives with Input.FutureValid {

  def foreach(func:TraversableOnce[In[Unit]]):Out[Unit] =
    Future.sequence(func).map(_.foldLeft[ValidationNel[ValidationFailure, Unit]](Success(Unit)) {
      case (v, Success(_)) =>
        v
      case (Failure(v), Failure(vf)) =>
        Failure(v.append(vf))
      case (_,c) =>
        c
    })

  def onSuccess[S, T](value: => In[S])(f : (S) => Out[T]): Out[T] =
    value.flatMap{
      case Success(s) =>
        f(s)
      case Failure(vf) =>
        failures(vf)
    }
}

