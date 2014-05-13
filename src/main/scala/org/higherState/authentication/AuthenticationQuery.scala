package org.higherState.authentication

import org.higherState.cqrs.Query

trait AuthenticationQuery extends Query[AuthenticationQueryParameters] with AuthenticationDirectives {

  def execute: Function[AuthenticationQueryParameters, Out[Any]] = {
    case Authenticate(userLogin, password) =>
      withRequiredAuthenticatedCredentials(userLogin, password) {
        case UserCredentials(actualUserLogin, _, true, _, _) =>
          failure(UserLockedFailure(actualUserLogin))
        case UserCredentials(actualUserLogin, _, _, false, _) =>
          failure(PasswordChangeRequiredFailure(actualUserLogin))
        case UserCredentials(actualUserLogin, _, _, _, _) =>
          result(actualUserLogin)
      }

    case GetLockedUserLogins =>
      result(service.getLockedUserLogins)
  }
}
