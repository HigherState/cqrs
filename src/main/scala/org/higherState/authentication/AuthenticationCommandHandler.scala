package org.higherState.authentication

import org.higherState.cqrs.CommandHandler

/**
 * Created by Gelfling on 23/03/14.
 */
trait AuthenticationCommandHandler extends CommandHandler with AuthenticationDirectives {

  type C = AuthenticationCommand

//  def handle = {
//    case CreateNewUser(userLogin, password) =>
//
//  }
}
