package org.higherState.authentication

import org.higherState.cqrs.{Validator, ServicePipe}
import org.higherState.repository.KeyValueRepository
import scalaz.~>

trait AuthenticationDirectives[In[+_], Out[+_]] extends Validator[ValidationFailure, Out] {

  import ServicePipe._

  implicit protected def pipe: ~>[In, Out]
  protected def repository:KeyValueRepository[In, UserLogin, UserCredentials]

  protected def withValidUniqueLogin[T](userLogin:UserLogin)(f: => Out[T]):Out[T] =
    bind(repository.get(userLogin)) {
      case Some(uc) =>
        failure(UserCredentialsAlreadyExistFailure(userLogin))
      case _ =>
        f
    }

  protected def withRequiredCredentials[T](userLogin:UserLogin)(f: UserCredentials => Out[T]):Out[T] =
    bind(repository.get(userLogin)) {
      case Some(uc) =>
        f(uc)
      case None =>
        failure(UserCredentialsNotFoundFailure(userLogin))
    }

  protected def withRequiredAuthenticatedCredentials[T](userLogin:UserLogin, password:Password)(f:UserCredentials => Out[T]):Out[T] =
    withRequiredCredentials(userLogin) { uc =>
      if (uc.password.isMatch(password))
        f(uc)
      else {
        //Event publisher, publish failure of authentication
        failure(InvalidPasswordFailure(userLogin))
      }
    }
}
