package org.higherState.authentication

import org.higherState.cqrs._
import org.higherState.cqrs.Service
import scala.collection.mutable

trait AuthenticationRepository extends Service {

  def getUserCredentials(userLogin:UserLogin):Out[Option[UserCredentials]]

  def getLockedUserLogins:Out[Iter[(UserLogin, Int)]]

  def getUserCredentialsByToken(token:ResetToken):Out[Option[UserCredentials]]

  def addUserCredentials(userCredentials:UserCredentials):Out[Unit]

  def setPassword(userLogin:UserLogin, password:Password, isResetRequired:Boolean):Out[Unit]

  def setToken(userLogin:UserLogin, token:ResetToken):Out[Unit]

  def setLock(userLogin:UserLogin, isLocked:Boolean):Out[Unit]

  def deleteUser(userLogin:UserLogin):Out[Unit]

  def getFailureCount(userLogin:UserLogin):Out[Option[Int]]

  def setFailureCount(userLogin:UserLogin, value:Int):Out[Unit]

}

trait InMemoryAuthenticationRepository extends AuthenticationRepository with Output.Identity {

  def state:mutable.Map[UserLogin, (UserCredentials, Int)]

  def getUserCredentials(userLogin:UserLogin):Option[UserCredentials] =
    state.get(userLogin).map(_._1)

  def getLockedUserLogins:Iter[(UserLogin, Int)] =
    state.values.filter(_._1.isLocked).map(p => p._1.userLogin -> p._2)

  def getUserCredentialsByToken(token:ResetToken):Option[UserCredentials] =
    state.values.collectFirst{
      case (uc, _) if uc.token.exists(_ == token) => uc
    }

  def addUserCredentials(userCredentials:UserCredentials) {
    state += userCredentials.userLogin -> (userCredentials -> 0)
  }

  def setPassword(userLogin:UserLogin, password:Password, isResetRequired:Boolean) {
    state.get(userLogin).map {
      case (uc, _) =>
        state += userLogin -> (uc.copy(password = password, isResetRequired = isResetRequired, token = None) -> 0)
    }
  }

  def setToken(userLogin:UserLogin, token:ResetToken) {
    state.get(userLogin).map {
      case (uc, failureCount) =>
        state += userLogin -> (uc.copy(token = Some(token)) -> failureCount)
    }
  }

  def setLock(userLogin:UserLogin, isLocked:Boolean) {
    state.get(userLogin).map {
      case (uc, failureCount) =>
        state += userLogin -> (uc.copy(isLocked = isLocked) -> 0)
    }
  }

  def deleteUser(userLogin:UserLogin) {
    state -= userLogin
  }

  def getFailureCount(userLogin:UserLogin) =
    state.get(userLogin).map(_._2)

  def setFailureCount(userLogin:UserLogin, value:Int) {
    state.get(userLogin).map {
      case (uc, failureCount) =>
        state += userLogin -> (uc -> value)
    }
  }
}
