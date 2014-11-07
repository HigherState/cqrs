package org.higherState.authentication

import org.higherState.cqrs.CommandHandler
import org.higherState.cqrs2

trait AuthenticationCommandHandler[In[+_], Out[+_]] extends cqrs2.CommandHandler[Out, AuthenticationCommand] with AuthenticationDirectives[In, Out] {

  def maxNumberOfTries:Int

  def handle = {
    case CreateNewUser(userLogin, password) =>
      withValidUniqueLogin(userLogin) {
          pipe(repository += (userLogin -> (
            UserCredentials(
              userLogin,
              password,
              false,
              0
            ))
          )
        )
      }

    case UpdatePasswordWithCurrentPassword(userLogin, currentPassword, newPassword) =>
      withRequiredAuthenticatedCredentials(userLogin, currentPassword) { uc =>
        pipe(repository += uc.userLogin -> uc.copy(password = newPassword, isLocked = false))
      }

    case DeleteUser(userLogin) =>
      withRequiredCredentials(userLogin) { uc =>
        pipe(repository -= uc.userLogin)
      }


    case SetLock(userLogin, isLocked) =>
      withRequiredCredentials(userLogin) { uc =>
        pipe(repository += uc.userLogin -> uc.copy(isLocked = isLocked))
      }

    case IncrementFailureCount(userLogin) =>
      map(pipe(repository.get(userLogin))) {
        case Some(uc) =>
          val newCount = uc.failureCount + 1
          pipe(repository += uc.userLogin -> uc.copy(failureCount = newCount, isLocked = newCount >= maxNumberOfTries))
        case None =>
          complete
      }

    case ResetFailureCount(userLogin) =>
      map(pipe(repository.get(userLogin))) {
        case Some(uc) =>
          pipe(repository += uc.userLogin -> uc.copy(failureCount = 0))
        case None =>
          complete
      }
  }
}
