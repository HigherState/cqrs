package org.higherState.authentication

import org.higherState.cqrs.{Service, CQDispatcher, Iter}

class AuthenticationService[Out[+_]](dispatcher:CQDispatcher[Out, AuthenticationCommand, AuthenticationQueryParameters]) extends Service[Out] {

  def createNewUser(userLogin:UserLogin, password:Password) =
    dispatcher.sendCommand(CreateNewUser(userLogin, password))

  def deleteUser(userLogin:UserLogin) =
    dispatcher.sendCommand(DeleteUser(userLogin))

  def incrementFailureCount(userLogin:UserLogin) =
    dispatcher.sendCommand(IncrementFailureCount(userLogin))

  def resetFailureCount(userLogin:UserLogin) =
    dispatcher.sendCommand(ResetFailureCount(userLogin))

  def setLock(userLogin:UserLogin, lock:Boolean) =
    dispatcher.sendCommand(SetLock(userLogin, lock))

  def updatePasswordWithCurrentPassword(userLogin:UserLogin, currentPassword:Password, newPassword:Password) =
    dispatcher.sendCommand(UpdatePasswordWithCurrentPassword(userLogin, currentPassword, newPassword))

  def authenticate(userLogin:UserLogin, password:Password):Out[UserLogin] =
    dispatcher.executeQuery[UserLogin](Authenticate(userLogin, password))

  def getLockedUserLogins =
    dispatcher.executeQuery[Iter[(UserLogin, Int)]](GetLockedUserLogins)

}
