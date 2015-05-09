package org.higherState.authentication

import org.higherState.cqrs.QueryParameters

sealed trait AuthenticationQueryParameters[R] extends QueryParameters[R]

case class Authenticate(userLogin:UserLogin, password:Password) extends AuthenticationQueryParameters[UserLogin]

case object GetLockedUserLogins extends AuthenticationQueryParameters[TraversableOnce[UserCredentials]]