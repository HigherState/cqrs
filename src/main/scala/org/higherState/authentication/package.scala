package org.higherState

import java.util.UUID

package object authentication {

  case class Password(value:String) extends AnyVal

  case class Salt(value:String) extends AnyVal

  case class Hash(value:String) extends AnyVal

  case class UserLogin(value:String) extends AnyVal

  case class ResetToken(uuid:UUID) extends AnyVal

  case class UserCredentials(
                              userLogin:UserLogin,
                              hash:Hash,
                              salt:Salt,
                              isLocked:Boolean,
                              isResetRequired:Boolean,
                              token:Option[ResetToken])
}
