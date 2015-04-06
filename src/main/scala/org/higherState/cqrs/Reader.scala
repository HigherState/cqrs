package org.higherState.cqrs

case class Reader[F, +T](wrappedF: F => T) {

  def apply(c: F) = wrappedF(c)

  def map[S](transformF: T => S): Reader[F, S] =
    Reader(c => transformF(wrappedF(c)))

  def flatMap[S](transformF: T => Reader[F, S]): Reader[F, S] =
    Reader(c => transformF(wrappedF(c))(c))

}