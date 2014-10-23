package org.higherState.authentication

import org.higherState.cqrs.CommandHandler

trait AuthenticationCommandHandler extends CommandHandler[AuthenticationCommand] with AuthenticationDirectives {

  def maxNumberOfTries:Int

  def handle = {
    case CreateNewUser(userLogin, password) =>
      withValidUniqueLogin(userLogin) {
          repository(_.addUserCredentials(
            UserCredentials(
              userLogin,
              password,
              false,
              false,
              None
            )
          )
        )
      }
    case CreateNewUserWithToken(userLogin) =>
      withValidUniqueLogin(userLogin) {
        val token = ResetToken(java.util.UUID.randomUUID())
        repository(_.addUserCredentials(
            UserCredentials(
              userLogin,
              Password.unSet,
              false,
              true,
              Some(token)
            )
          )
        )
      }

    case UpdatePasswordWithCurrentPassword(userLogin, currentPassword, newPassword) =>
      withRequiredAuthenticatedCredentials(userLogin, currentPassword) { uc =>
        repository(_.setPassword(uc.userLogin, newPassword, false))
      }

    case UpdatePasswordWithToken(token, password) =>
      withCredentialsByToken(token) { uc =>
        repository(_.setPassword(uc.userLogin, password, false))
      }

    case DeleteUser(userLogin) =>
      withRequiredCredentials(userLogin) { uc =>
        repository(_.deleteUser(uc.userLogin))
      }

    case RequestResetToken(userLogin) =>
      withRequiredCredentials(userLogin) { uc =>
        val token = ResetToken(java.util.UUID.randomUUID())
        repository(_.setToken(userLogin, token))
      }

    case SetLock(userLogin, isLocked) =>
      withRequiredCredentials(userLogin) { uc =>
        repository(_.setLock(userLogin, isLocked))
      }

    case IncrementFailureCount(userLogin) =>
      map(repository(_.getFailureCount(userLogin))) {
        case Some(failureCount) =>
          val newCount = failureCount + 1
          map(repository(_.setFailureCount(userLogin, newCount))) { unit =>
            if (newCount == maxNumberOfTries)
              repository(_.setLock(userLogin, true))
            else
              complete
          }
        case None =>
          complete
      }

    case ResetFailureCount(userLogin) =>
      repository(_.setFailureCount(userLogin, 0))
  }
}
