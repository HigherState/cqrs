package org.higherState.authentication

import org.higherState.cqrs.{Pipe, ServicePipe, QueryExecutor}
import org.higherState.repository.KeyValueRepository
import scalaz.Monad

abstract class AuthenticationQueryExecutor[In[+_], Out[+_]]
  (val repository:KeyValueRepository[In, UserLogin, UserCredentials])
  (implicit val pipe:Pipe[In, Out], val fm:Monad[Out])
  extends QueryExecutor[Out, AuthenticationQueryParameters] with AuthenticationDirectives[In, Out] {

  import ServicePipe._

  def execute = {
    case Authenticate(userLogin, password) =>
      withRequiredAuthenticatedCredentials(userLogin, password) {
        case UserCredentials(actualUserLogin, _, true, _) =>
          failure(UserLockedFailure(actualUserLogin))
        case UserCredentials(actualUserLogin, _, _, _) =>
          unit(actualUserLogin)
      }

    case GetLockedUserLogins =>
      map(repository.values) { credentials =>
        credentials.filter(_.isLocked)
      }
  }
}
