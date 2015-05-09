package org.higherState.authentication

import org.higherState.cqrs._
import org.higherState.repository.KeyValueRepository

class AuthenticationCommandHandler[Out[+_]:VMonad, In[+_]:(~>![Out])#I]
  (repository:KeyValueRepository[In, UserLogin, UserCredentials], maxNumberOfTries:Int)
  extends AuthenticationDirectives[Out, In](repository) with CommandHandler[Out, AuthenticationCommand]{

  import ServicePipe._

  def handle = {
    case CreateNewUser(userLogin, password) =>
      withValidUniqueLogin(userLogin) {
        repository += (userLogin -> (
          UserCredentials(
            userLogin,
            password,
            false,
            0
          ))
        )
      }

    case UpdatePasswordWithCurrentPassword(userLogin, currentPassword, newPassword) =>
      withRequiredAuthenticatedCredentials(userLogin, currentPassword) { uc =>
        repository += uc.userLogin -> uc.copy(password = newPassword, isLocked = false)
      }

    case DeleteUser(userLogin) =>
      withRequiredCredentials(userLogin) { uc =>
        repository -= uc.userLogin
      }


    case SetLock(userLogin, isLocked) =>
      withRequiredCredentials(userLogin) { uc =>
        repository += uc.userLogin -> uc.copy(isLocked = isLocked)
      }

    case IncrementFailureCount(userLogin) =>
      bind(repository.get(userLogin)) {
        case Some(uc) =>
          val newCount = uc.failureCount + 1
          repository += uc.userLogin -> uc.copy(failureCount = newCount, isLocked = newCount >= maxNumberOfTries)
        case None =>
          acknowledged
      }

    case ResetFailureCount(userLogin) =>
      bind(repository.get(userLogin)) {
        case Some(uc) =>
          repository += uc.userLogin -> uc.copy(failureCount = 0)
        case None =>
          acknowledged
      }
  }
}
