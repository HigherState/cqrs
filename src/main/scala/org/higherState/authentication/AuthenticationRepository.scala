package org.higherState.authentication

import org.higherState.cqrs._
import org.higherState.cqrs.Service
import scala.collection.mutable

trait AuthenticationRepository extends Service {

  def getUserCredentials(userLogin:UserLogin):Out[Option[UserCredentials]]

  def getLockedUserLogins:Out[TraversableOnce[UserLogin]]

  def getUserCredentialsByToken(token:ResetToken):Out[Option[UserCredentials]]


  def addUserCredentials(userCredentials:UserCredentials):Out[Unit]

  def setPassword(userLogin:UserLogin, password:Password, isResetRequired:Boolean):Out[Unit]

  def setToken(userLogin:UserLogin, token:ResetToken):Out[Unit]

  def setLock(userLogin:UserLogin, isLocked:Boolean):Out[Unit]

  def deleteUser(userLogin:UserLogin):Out[Unit]

}

trait InMemoryAuthenticationRepository extends AuthenticationRepository with Output.Identity {

  def state:mutable.Map[UserLogin, UserCredentials]

  def getUserCredentials(userLogin:UserLogin):Option[UserCredentials] =
    state.get(userLogin)

  def getLockedUserLogins:TraversableOnce[UserLogin] =
    state.values.filter(_.isLocked).map(_.userLogin)

  def getUserCredentialsByToken(token:ResetToken):Option[UserCredentials] =
    state.values.find(p => p.token.exists(_ == token))


  def addUserCredentials(userCredentials:UserCredentials) {
    state += userCredentials.userLogin -> userCredentials
  }

  def setPassword(userLogin:UserLogin, password:Password, isResetRequired:Boolean) {
    state.get(userLogin).map {uc =>
      state += userLogin -> uc.copy(password = password, isResetRequired = isResetRequired, token = None)
    }
  }

  def setToken(userLogin:UserLogin, token:ResetToken) {
    state.get(userLogin).map {uc =>
      state += userLogin -> uc.copy(token = Some(token))
    }
  }

  def setLock(userLogin:UserLogin, isLocked:Boolean) {
    state.get(userLogin).map {uc =>
      state += userLogin -> uc.copy(isLocked = isLocked)
    }
  }

  def deleteUser(userLogin:UserLogin) {
    state -= userLogin
  }
}
