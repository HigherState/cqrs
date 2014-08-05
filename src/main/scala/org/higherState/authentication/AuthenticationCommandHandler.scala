package org.higherState.authentication

import org.higherState.cqrs.CommandHandler

trait AuthenticationCommandHandler extends CommandHandler[AuthenticationCommand] with AuthenticationDirectives {

  def maxNumberOfTries:Int

  def handle = {
    case CreateNewUser(userLogin, password) =>
      withValidUniqueLogin(userLogin) {
          servicePipe.transform(_.addUserCredentials(
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
        servicePipe.transform(_.addUserCredentials(
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
        servicePipe.transform(_.setPassword(uc.userLogin, newPassword, false))
      }

    case UpdatePasswordWithToken(token, password) =>
      withCredentialsByToken(token) { uc =>
        servicePipe.transform(_.setPassword(uc.userLogin, password, false))
      }

    case DeleteUser(userLogin) =>
      withRequiredCredentials(userLogin) { uc =>
        servicePipe.transform(_.deleteUser(uc.userLogin))
      }

    case RequestResetToken(userLogin) =>
      withRequiredCredentials(userLogin) { uc =>
        val token = ResetToken(java.util.UUID.randomUUID())
        servicePipe.transform(_.setToken(userLogin, token))
      }

    case SetLock(userLogin, isLocked) =>
      withRequiredCredentials(userLogin) { uc =>
        servicePipe.transform(_.setLock(userLogin, isLocked))
      }

    case IncrementFailureCount(userLogin) =>
      servicePipe(_.getFailureCount(userLogin)) {
        case Some(failureCount) =>
          val newCount = failureCount + 1
          servicePipe(_.setFailureCount(userLogin, newCount)) { unit =>
            if (newCount == maxNumberOfTries)
              servicePipe.transform(_.setLock(userLogin, true))
            else
              complete
          }
        case None =>
          complete
      }

    case ResetFailureCount(userLogin) =>
      servicePipe.transform(_.setFailureCount(userLogin, 0))
  }
}
