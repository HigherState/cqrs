package org.higherState.authentication

import org.higherState.cqrs.directives.ValidationDirectives

trait AuthenticationDirectives extends ValidationDirectives {
  d =>

  def repository:AuthenticationRepository  { type R[T] = d.R[T] }

  def doesNotExist[T](userLogin:UserLogin)(f: => R[T]):R[T] =
    conditional(repository.getUserCredentials(userLogin), )


}
