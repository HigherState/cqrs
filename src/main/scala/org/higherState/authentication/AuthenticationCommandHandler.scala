package org.higherState.authentication

import org.higherState.cqrs.CommandHandler

trait AuthenticationCommandHandler extends CommandHandler[AuthenticationCommand] with AuthenticationDirectives {

  def maxNumberOfTries:Int

  def handle = {
    case CreateNewUser(userLogin, password) =>
      withValidUniqueLogin(userLogin) {
          repository(_ += (userLogin -> (
            UserCredentials(
              userLogin,
              password,
              false,
              0
            ))
          )
        )
      }

    case UpdatePasswordWithCurrentPassword(userLogin, currentPassword, newPassword) =>
      withRequiredAuthenticatedCredentials(userLogin, currentPassword) { uc =>
        repository(_ += uc.userLogin -> uc.copy(password = newPassword, isLocked = false))
      }

    case DeleteUser(userLogin) =>
      withRequiredCredentials(userLogin) { uc =>
        repository(_ -= uc.userLogin)
      }


    case SetLock(userLogin, isLocked) =>
      withRequiredCredentials(userLogin) { uc =>
        repository(_ += uc.userLogin -> uc.copy(isLocked = isLocked))
      }

    case IncrementFailureCount(userLogin) =>
      map(repository(_.get(userLogin))) {
        case Some(uc) =>
          val newCount = uc.failureCount + 1
          map(repository(_ += uc.userLogin -> uc.copy(failureCount = newCount, isLocked = newCount >= maxNumberOfTries)))
        case None =>
          complete
      }

    case ResetFailureCount(userLogin) =>
      map(repository(_.get(userLogin))) {
        case Some(uc) =>
          map(repository(_ += uc.userLogin -> uc.copy(failureCount = 0)))
        case None =>
          complete
      }
  }
}
