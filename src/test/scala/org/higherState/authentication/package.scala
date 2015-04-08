package org.higherState

import java.util.UUID

import org.higherState.cqrs.FMonad

package object authentication {

  type VMonad[Out[+_]] = FMonad[ValidationFailure, Out]

  /* illustrative only, passwords not hashed*/
  case class Password(value:String) extends AnyVal {
    def isUnset = value.isEmpty

    def isMatch(password:Password) =
      !isUnset && password.value == value
  }
  object Password {
    def unSet = Password("")
  }

  case class UserLogin(value:String) extends AnyVal

  case class ResetToken(uuid:UUID) extends AnyVal

  case class UserCredentials(
                              userLogin:UserLogin,
                              password:Password,
                              isLocked:Boolean,
                              failureCount:Int)
}
