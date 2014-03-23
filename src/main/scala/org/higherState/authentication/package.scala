package org.higherState

import java.util.UUID

package object authentication {

  case class Password(value:String) extends AnyVal

  case class UserLogin(value:String) extends AnyVal

  case class ResetToken(uuid:UUID) extends AnyVal

  case class UserCredentials(
                              userLogin:UserLogin,
                              password:Password,
                              isLocked:Boolean,
                              isResetRequired:Boolean,
                              token:Option[ResetToken])
}
