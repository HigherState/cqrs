package org.higherState.cqrs

object SequenceHelper {
  import scalaz.syntax.traverse._
  import scalaz.std.list._
  def apply[T](self:TraversableOnce[Valid[T]]):Valid[List[T]] =
    self.toList.sequence
}
