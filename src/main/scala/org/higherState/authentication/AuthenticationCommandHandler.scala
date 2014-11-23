package org.higherState.authentication

import org.higherState.cqrs.{ ServicePipe, CommandHandler}

trait AuthenticationCommandHandler[In[+_], Out[+_]] extends CommandHandler[Out, AuthenticationCommand] with AuthenticationDirectives[In, Out] {

  import ServicePipe._

  protected def maxNumberOfTries:Int

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
      map(repository.get(userLogin)) {
        case Some(uc) =>
          val newCount = uc.failureCount + 1
          repository += uc.userLogin -> uc.copy(failureCount = newCount, isLocked = newCount >= maxNumberOfTries)
        case None =>
          complete
      }

    case ResetFailureCount(userLogin) =>
      map(repository.get(userLogin)) {
        case Some(uc) =>
          repository += uc.userLogin -> uc.copy(failureCount = 0)
        case None =>
          complete
      }
  }
}
