package org.higherState.authentication

import org.higherState.cqrs._
import org.higherState.repository.KeyValueRepository

class AuthenticationQueryExecutor[Out[+_]:VMonad, In[+_]:(~>![Out])#I]
  (repository:KeyValueRepository[In, UserLogin, UserCredentials])
  extends AuthenticationDirectives[Out, In](repository) with QueryExecutor[Out, AuthenticationQueryParameters] {

  import VMonad._

  def execute[T] = {
    case Authenticate(userLogin, password) =>
      withRequiredAuthenticatedCredentials(userLogin, password) {
        case UserCredentials(actualUserLogin, _, true, _) =>
          failure("UserLockedFailure(actualUserLogin)")
        case UserCredentials(actualUserLogin, _, _, _) =>
          point(actualUserLogin)
      }

    case IsLocked(userLogin) =>
      for {
        mc <- repository.get(userLogin)
      } yield mc.exists(_.isLocked)

    case GetLockedUserLogins =>
      repository.values.map { credentials =>
        credentials.filter(_.isLocked)
      }
  }
}
