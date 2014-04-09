package org.higherState.authentication

import org.higherState.cqrs.CommandHandler
//
//trait AuthenticationCommandHandler extends CommandHandler with AuthenticationDirectives {
//
//  type C = AuthenticationCommand
//
//  def handle = {
//    case CreateNewUser(userLogin, password) =>
//      uniqueCheck(userLogin) {
//        complete{
//          repository.addUserCredentials(
//            UserCredentials(
//              userLogin,
//              password,
//              false,
//              false,
//              None
//            )
//          )
//        }
//      }
//    case CreateNewUserWithToken(userLogin) =>
//      uniqueCheck(userLogin) {
//        complete{
//          repository.addUserCredentials(
//            UserCredentials(
//              userLogin,
//              Password(""),
//              false,
//              false,
//              None
//            )
//          )
//        }
//      }
//  }
//}
