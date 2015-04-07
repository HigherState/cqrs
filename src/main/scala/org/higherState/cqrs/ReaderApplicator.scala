package org.higherState.cqrs

/**
 * Created by jamie.pullar on 07/04/2015.
 */
trait ReaderApplicator[F] {
  def apply[T](reader:Reader[F, T]):T
}
