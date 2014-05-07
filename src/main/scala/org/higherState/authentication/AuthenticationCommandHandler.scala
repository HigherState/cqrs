package org.higherState.authentication

import org.higherState.cqrs.CommandHandler

trait AuthenticationCommandHandler extends CommandHandler[AuthenticationCommand] with AuthenticationDirectives {

  def handle = {
    case CreateNewUser(userLogin, password) =>
      withValidUniqueLogin(userLogin) {
        onSuccessComplete{
          service.addUserCredentials(
            UserCredentials(
              userLogin,
              password,
              false,
              false,
              None
            )
          )
        }
      }
    case CreateNewUserWithToken(userLogin) =>
      withValidUniqueLogin(userLogin) {
        val token = ResetToken(java.util.UUID.randomUUID())
        onSuccessComplete{
          service.addUserCredentials(
            UserCredentials(
              userLogin,
              Password.unSet,
              false,
              true,
              Some(token)
            )
          )
        }
      }

    case UpdatePasswordWithCurrentPassword(userLogin, currentPassword, newPassword) =>
      withRequiredAuthenticatedCredentails(userLogin, currentPassword) { uc =>
        onSuccessComplete{
          service.setPassword(uc.userLogin, newPassword, false)
        }
      }

    case UpdatePasswordWithToken(token, password) =>
      withCredentialsByToken(token) { uc =>
        onSuccessComplete{
          service.setPassword(uc.userLogin, password, false)
        }
      }

    case DeleteUser(userLogin) =>
      withRequiredCredentials(userLogin) { uc =>
        onSuccessComplete{
          service.deleteUser(uc.userLogin)
        }
      }

    case RequestResetToken(userLogin) =>
      withRequiredCredentials(userLogin) { uc =>
        val token = ResetToken(java.util.UUID.randomUUID())
        onSuccess(service.setToken(userLogin, token)) { unit =>
          //publish token to event Publisher
          complete
        }
      }

    case SetLock(userLogin, isLocked) =>
      withRequiredCredentials(userLogin) { uc =>
        onSuccessComplete{
          service.setLock(userLogin, isLocked)
        }
      }
  }
}
