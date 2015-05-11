package org.higherState.authentication

import org.higherState.cqrs._
import org.higherState.repository.KeyValueRepository

abstract class AuthenticationDirectives[Out[+_]:VMonad, In[+_]:(~>![Out])#I]
  (repository:KeyValueRepository[In, UserLogin, UserCredentials]) {

  import VMonad._


  protected def withValidUniqueLogin[T](userLogin:UserLogin)(f: => Out[T]):Out[T] =
    repository.get(userLogin).flatMap {
      case Some(uc) =>
        failure("UserCredentialsAlreadyExistFailure(userLogin)")
      case _ =>
        f
    }

  protected def withRequiredCredentials[T](userLogin:UserLogin)(f: UserCredentials => Out[T]):Out[T] =
    repository.get(userLogin).flatMap {
      case Some(uc) =>
        f(uc)
      case None =>
        failure("UserCredentialsNotFoundFailure(userLogin)")
    }

  protected def withRequiredAuthenticatedCredentials[T](userLogin:UserLogin, password:Password)(f:UserCredentials => Out[T]):Out[T] =
    withRequiredCredentials(userLogin) { uc =>
      if (uc.password.isMatch(password))
        f(uc)
      else {
        //Event publisher, publish failure of authentication
        failure("InvalidPasswordFailure(userLogin)")
      }
    }
}
