package org.higherState.authentication

import org.higherState.cqrs.CommandHandler

trait AuthenticationCommandHandler extends CommandHandler[AuthenticationCommand] with AuthenticationDirectives {

  def handle = {
    case CreateNewUser(userLogin, password) =>
      uniqueCheck(userLogin) {
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
      uniqueCheck(userLogin) {
        onSuccessComplete{
          service.addUserCredentials(
            UserCredentials(
              userLogin,
              Password(""),
              false,
              false,
              None
            )
          )
        }
      }
  }
}
