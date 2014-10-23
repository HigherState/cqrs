package org.higherState.authentication

import org.higherState.cqrs.ValidationDirectives

trait AuthenticationDirectives extends ValidationDirectives {

  def repository:Pipe[AuthenticationRepository]

  def withValidUniqueLogin[T](userLogin:UserLogin)(f: => Out[T]):Out[T] =
    flatMap(repository(_.getUserCredentials(userLogin))) {
      case Some(uc) =>
        failure(UserCredentialsAlreadyExistFailure(userLogin))
      case _ =>
        f
    }

  def withRequiredCredentials[T](userLogin:UserLogin)(f: UserCredentials => Out[T]):Out[T] =
    flatMap(repository(_.getUserCredentials(userLogin))) {
      case Some(uc) =>
        f(uc)
      case None =>
        failure(UserCredentialsNotFoundFailure(userLogin))
    }

  def withRequiredAuthenticatedCredentials[T](userLogin:UserLogin, password:Password)(f:UserCredentials => Out[T]):Out[T] =
    withRequiredCredentials(userLogin) { uc =>
      if (uc.password.isMatch(password))
        f(uc)
      else {
        //Event publisher, publish failure of authentication
        failure(InvalidPasswordFailure(userLogin))
      }
    }

  def withCredentialsByToken[T](token:ResetToken)(f:UserCredentials => Out[T]):Out[T] =
    flatMap(repository(_.getUserCredentialsByToken(token))) {
      case Some(uc) =>
        f(uc)
      case None =>
        failure(TokenExpiredFailure(token))
    }
}
