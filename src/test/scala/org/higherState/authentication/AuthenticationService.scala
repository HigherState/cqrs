package org.higherState.authentication

import org.higherState.cqrs.{CommandQueryController, Service}

class AuthenticationService[Out[+_]](controller:CommandQueryController[Out, AuthenticationCommand, AuthenticationQueryParameters]) extends Service[Out] {

  def createNewUser(userLogin:UserLogin, password:Password) =
    controller.sendCommand(CreateNewUser(userLogin, password))

  def deleteUser(userLogin:UserLogin) =
    controller.sendCommand(DeleteUser(userLogin))

  def incrementFailureCount(userLogin:UserLogin) =
    controller.sendCommand(IncrementFailureCount(userLogin))

  def resetFailureCount(userLogin:UserLogin) =
    controller.sendCommand(ResetFailureCount(userLogin))

  def setLock(userLogin:UserLogin, lock:Boolean) =
    controller.sendCommand(SetLock(userLogin, lock))

  def updatePasswordWithCurrentPassword(userLogin:UserLogin, currentPassword:Password, newPassword:Password) =
    controller.sendCommand(UpdatePasswordWithCurrentPassword(userLogin, currentPassword, newPassword))

  def authenticate(userLogin:UserLogin, password:Password) =
    controller.executeQuery(Authenticate(userLogin, password))

  def getLockedUserLogins =
    controller.executeQuery(GetLockedUserLogins)

}
