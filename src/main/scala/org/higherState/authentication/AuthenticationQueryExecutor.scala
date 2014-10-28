package org.higherState.authentication

import org.higherState.cqrs.QueryExecutor

trait AuthenticationQueryExecutor extends QueryExecutor[AuthenticationQueryParameters] with AuthenticationDirectives {

  def execute: Function[AuthenticationQueryParameters, Out[Any]] = {
    case Authenticate(userLogin, password) =>
      withRequiredAuthenticatedCredentials(userLogin, password) {
        case UserCredentials(actualUserLogin, _, true, _) =>
          failure(UserLockedFailure(actualUserLogin))
        case UserCredentials(actualUserLogin, _, _, _) =>
          unit(actualUserLogin)
      }

    case GetLockedUserLogins =>
      map(repository(_.values)) { credentials =>
        credentials.filter(_.isLocked)
      }
  }
}
