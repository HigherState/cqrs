package org.higherState.authentication

import org.higherState.cqrs.FMonad

trait ValidationFailure

case class ValidationException(failure:ValidationFailure) extends Throwable

trait VMonadBind[Out[+_]] extends org.higherState.cqrs.FMonadBind[ValidationFailure, Out]

abstract class VMonadBound[Out[+_]:VMonad] extends VMonadBind[Out] {
  protected def monad: FMonad[ValidationFailure, Out] = implicitly[VMonad[Out]]
}