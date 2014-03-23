package org.higherState.authentication

import org.higherState.cqrs.directives.{Bridge, ValidationDirectives}

trait AuthenticationDirectives extends ValidationDirectives with Bridge {
  d =>

  def repository:AuthenticationRepository  { type R[T] = d.R2[T] }

  def uniqueCheck[T](userLogin:UserLogin)(f: => R[T]):R[T] =
    onSuccess(repository.getUserCredentials(userLogin)) {
      case Some(uc) =>
        failure(UserCredentialsAlreadyExistFailure(userLogin))
      case _ =>
        f
    }



}
