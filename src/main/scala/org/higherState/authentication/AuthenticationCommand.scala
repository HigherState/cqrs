package org.higherState.authentication

import org.higherState.cqrs.Command

sealed trait AuthenticationCommand extends Command

case class DeleteUser(userLogin:UserLogin) extends AuthenticationCommand

case class UpdatePasswordWithCurrentPassword(userLogin:UserLogin, currentPassword:Password, newPassword:Password) extends AuthenticationCommand

case class UpdatePasswordWithToken(token:Token, password:Password) extends AuthenticationCommand

case class CreateNewUser(userLogin:UserLogin, password:Password) extends AuthenticationCommand

case class CreateNewUserWithToken(userLogin:UserLogin) extends AuthenticationCommand

case class CreateNewUserWithRandomPassword(userLogin:UserLogin, resetRequired:Boolean) extends AuthenticationCommand

case class RequestResetToken(userLogin:UserLogin) extends AuthenticationCommand

case class RemoveLock(userLogin:UserLogin) extends AuthenticationCommand


