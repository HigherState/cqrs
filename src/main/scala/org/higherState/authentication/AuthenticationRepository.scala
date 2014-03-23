package org.higherState.authentication

import org.higherState.cqrs.Service
import scala.collection.mutable

trait AuthenticationRepository extends Service {

  def getUserCredentials(userLogin:UserLogin):R[Option[UserCredentials]]

  def getLockedUserLogins:R[TraversableOnce[UserLogin]]

  def getUserCredentialsByToken(token:ResetToken):R[Option[UserCredentials]]


  def addUserCredentials(userCredentials:UserCredentials):R[Unit]

  def setHash(userLogin:UserLogin, hash:Hash, salt:Salt, resetRequired:Boolean):R[Unit]

  def setToken(userLogin:UserLogin, token:ResetToken):R[Unit]

  def setLock(userLogin:UserLogin, isLocked:Boolean):R[Unit]

  def deleteUser(userLogin:UserLogin):R[Unit]

}

trait InMemoryAuthenticationRepository extends AuthenticationRepository {

  type R[T] = T

  def state:mutable.Map[UserLogin, UserCredentials]

  def getUserCredentials(userLogin:UserLogin):R[Option[UserCredentials]] =
    state.get(userLogin)

  def getLockedUserLogins:R[TraversableOnce[UserLogin]] =
    state.values.filter(_.isLocked).map(_.userLogin)

  def getUserCredentialsByToken(token:ResetToken):R[Option[UserCredentials]] =
    state.values.find(p => p.token.exists(_ == token))


  def addUserCredentials(userCredentials:UserCredentials) {
    state += userCredentials.userLogin -> userCredentials
  }

  def setHash(userLogin:UserLogin, hash:Hash, salt:Salt, isResetRequired:Boolean) {
    state.get(userLogin).map {uc =>
      state += userLogin -> uc.copy(hash = hash, salt = salt, isResetRequired = isResetRequired)
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
