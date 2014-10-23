package org.higherState.authentication

import org.higherState.cqrs.QueryExecutor

trait AuthenticationQueryExecutor extends QueryExecutor[AuthenticationQueryParameters] with AuthenticationDirectives {

  def execute: Function[AuthenticationQueryParameters, Out[Any]] = {
    case Authenticate(userLogin, password) =>
      withRequiredAuthenticatedCredentials(userLogin, password) {
        case UserCredentials(actualUserLogin, _, true, _, _) =>
          failure(UserLockedFailure(actualUserLogin))
        case UserCredentials(actualUserLogin, _, _, false, _) =>
          failure(PasswordChangeRequiredFailure(actualUserLogin))
        case UserCredentials(actualUserLogin, _, _, _, _) =>
          unit(actualUserLogin)
      }

    case GetLockedUserLogins =>
      repository(_.getLockedUserLogins)
  }
}
