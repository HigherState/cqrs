package org.higherState.authentication

import org.higherState.cqrs.{PipedService, ValidationDirectives}

trait AuthenticationDirectives extends ValidationDirectives with PipedService[AuthenticationRepository] {

  def uniqueCheck[T](userLogin:UserLogin)(f: => Out[T]):Out[T] =
    onSuccess(service.getUserCredentials(userLogin)) {
      case Some(uc) =>
        failure(UserCredentialsAlreadyExistFailure(userLogin))
      case _ =>
        f
    }
}
