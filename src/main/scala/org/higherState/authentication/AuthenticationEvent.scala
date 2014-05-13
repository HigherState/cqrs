package org.higherState.authentication

import org.higherState.cqrs.Event

trait AuthenticationEvent extends Event

case class AuthenticationFailureEvent(userLogin:UserLogin) extends AuthenticationEvent

case class AuthenticationSuccessEvent(userLogin:UserLogin) extends AuthenticationEvent

case class NewUserCredentialsTokenEvent(userLogin:UserLogin, token:ResetToken) extends AuthenticationEvent

case class UserPasswordResetTokenEvent(userLogin:UserLogin, token:ResetToken) extends AuthenticationEvent
