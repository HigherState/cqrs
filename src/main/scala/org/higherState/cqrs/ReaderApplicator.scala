package org.higherState.cqrs


trait ReaderApplicator[F] {
  def apply[T](reader:Reader[F, T]):T
}
