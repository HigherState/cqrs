package org.higherState.authentication

import org.higherState.cqrs2

trait AuthenticationQueryExecutor[In[+_], Out[+_]] extends cqrs2.QueryExecutor[Out, AuthenticationQueryParameters] with AuthenticationDirectives[In, Out] {

  def execute: Function[AuthenticationQueryParameters, Out[Any]] = {
    case Authenticate(userLogin, password) =>
      withRequiredAuthenticatedCredentials(userLogin, password) {
        case UserCredentials(actualUserLogin, _, true, _) =>
          failure(UserLockedFailure(actualUserLogin))
        case UserCredentials(actualUserLogin, _, _, _) =>
          unit(actualUserLogin)
      }

    case GetLockedUserLogins =>
      map(pipe(repository.values)) { credentials =>
        credentials.filter(_.isLocked)
      }
  }
}
