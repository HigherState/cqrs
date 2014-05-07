package org.higherState.authentication

import org.higherState.cqrs.{PipedService, ValidationDirectives}

trait AuthenticationDirectives extends ValidationDirectives with PipedService[AuthenticationRepository] {

  def withValidUniqueLogin[T](userLogin:UserLogin)(f: => Out[T]):Out[T] =
    onSuccess(service.getUserCredentials(userLogin)) {
      case Some(uc) =>
        failure(UserCredentialsAlreadyExistFailure(userLogin))
      case _ =>
        f
    }

  def withRequiredCredentials[T](userLogin:UserLogin)(f: UserCredentials => Out[T]):Out[T] =
    onSuccess(service.getUserCredentials(userLogin)) {
      case Some(uc) =>
        f(uc)
      case None =>
        failure(UserCredentialsNotFoundFailure(userLogin))
    }

  def withRequiredAuthenticatedCredentails[T](userLogin:UserLogin, password:Password)(f:UserCredentials => Out[T]):Out[T] =
    withRequiredCredentials(userLogin) { uc =>
      if (uc.password.isMatch(password))
        f(uc)
      else failure(InvalidPasswordFailure(userLogin))
    }

  def withCredentialsByToken[T](token:ResetToken)(f:UserCredentials => Out[T]):Out[T] =
    onSuccess(service.getUserCredentialsByToken(token)) {
      case Some(uc) =>
        f(uc)
      case None =>
        failure(TokenExpiredFailure(token))
    }
}
