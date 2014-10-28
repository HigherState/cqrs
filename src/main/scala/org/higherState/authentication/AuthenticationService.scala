package org.higherState.authentication

import org.higherState.cqrs.{Iter, CqrsService}

trait AuthenticationService extends CqrsService[AuthenticationCommand, AuthenticationQueryParameters] {

  def createNewUser(userLogin:UserLogin, password:Password) =
    dispatchCommand(CreateNewUser(userLogin, password))

  def deleteUser(userLogin:UserLogin) =
    dispatchCommand(DeleteUser(userLogin))

  def incrementFailureCount(userLogin:UserLogin) =
    dispatchCommand(IncrementFailureCount(userLogin))

  def resetFailureCount(userLogin:UserLogin) =
    dispatchCommand(ResetFailureCount(userLogin))

  def setLock(userLogin:UserLogin, lock:Boolean) =
    dispatchCommand(SetLock(userLogin, lock))

  def updatePasswordWithCurrentPassword(userLogin:UserLogin, currentPassword:Password, newPassword:Password) =
    dispatchCommand(UpdatePasswordWithCurrentPassword(userLogin, currentPassword, newPassword))

  def authenticate(userLogin:UserLogin, password:Password):Out[UserLogin] =
     executeQuery[UserLogin](Authenticate(userLogin, password))

  def getLockedUserLogins =
    executeQuery[Iter[(UserLogin, Int)]](GetLockedUserLogins)

}
