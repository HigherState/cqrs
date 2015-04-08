package org.higherState.authentication

import org.higherState.cqrs.QueryParameters

sealed trait AuthenticationQueryParameters extends QueryParameters

case class Authenticate(userLogin:UserLogin, password:Password) extends AuthenticationQueryParameters

case object GetLockedUserLogins extends AuthenticationQueryParameters