package org.higherState.authentication

import org.higherState.cqrs.Command

sealed trait AuthenticationCommand extends Command

case class DeleteUser(userLogin:UserLogin) extends AuthenticationCommand

case class UpdatePasswordWithCurrentPassword(userLogin:UserLogin, currentPassword:Password, newPassword:Password) extends AuthenticationCommand

case class CreateNewUser(userLogin:UserLogin, password:Password) extends AuthenticationCommand

case class SetLock(userLogin:UserLogin, isLocked:Boolean) extends AuthenticationCommand

case class IncrementFailureCount(userLogin:UserLogin) extends AuthenticationCommand

case class ResetFailureCount(userLogin:UserLogin) extends AuthenticationCommand


