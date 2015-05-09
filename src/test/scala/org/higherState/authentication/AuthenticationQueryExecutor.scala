package org.higherState.authentication

import org.higherState.cqrs._
import org.higherState.repository.KeyValueRepository

class AuthenticationQueryExecutor[Out[+_]:VMonad, In[+_]:(~>![Out])#I]
  (repository:KeyValueRepository[In, UserLogin, UserCredentials])
  extends AuthenticationDirectives[Out, In](repository) with QueryExecutor[Out, AuthenticationQueryParameters] {

  import ServicePipe._

  def execute[T] = {
    case Authenticate(userLogin, password) =>
      withRequiredAuthenticatedCredentials(userLogin, password) {
        case UserCredentials(actualUserLogin, _, true, _) =>
          failure(UserLockedFailure(actualUserLogin))
        case UserCredentials(actualUserLogin, _, _, _) =>
          point(actualUserLogin)
      }

    case GetLockedUserLogins =>
      map(repository.values) { credentials =>
        credentials.filter(_.isLocked)
      }
  }
}
