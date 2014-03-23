package org.higherState.authentication

import org.higherState.cqrs.ValidationFailure

trait AuthenticationFailure extends ValidationFailure

case class UserLockedFailure(userLogin:UserLogin) extends AuthenticationFailure

case class UserCredentialsNotFoundFailure(userLogin:UserLogin) extends AuthenticationFailure

case class InvalidPasswordFailure(userLogin:UserLogin) extends AuthenticationFailure

case class PasswordChangeRequiredFailure(userLogin:UserLogin) extends AuthenticationFailure

case class UserCredentialsAlreadyExistFailure(userLogin:UserLogin) extends AuthenticationFailure

case class TokenExpiredFailure(token:ResetToken) extends AuthenticationFailure