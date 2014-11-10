package org.higherState.authentication

import org.higherState.cqrs.{ServicePipe, Pipe, FailureDirectives}
import org.higherState.repository.KeyValueRepository

trait AuthenticationDirectives[In[+_], Out[+_]] extends FailureDirectives[Out] {

  import ServicePipe._

  implicit protected def pipe:Pipe[In, Out]
  def repository:KeyValueRepository[In, UserLogin, UserCredentials]

  def withValidUniqueLogin[T](userLogin:UserLogin)(f: => Out[T]):Out[T] =
    flatMap(repository.get(userLogin)) {
      case Some(uc) =>
        failure(UserCredentialsAlreadyExistFailure(userLogin))
      case _ =>
        f
    }

  def withRequiredCredentials[T](userLogin:UserLogin)(f: UserCredentials => Out[T]):Out[T] =
    flatMap(repository.get(userLogin)) {
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
}
