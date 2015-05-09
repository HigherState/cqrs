package org.higherState.repository

import org.higherState.cqrs._
import java.util.concurrent.atomic.AtomicReference

final class InMemoryKeyValueQueryExecutor[Out[+_]:Monad, Key, Value](state:AtomicReference[Map[Key, Value]])
  extends QueryExecutor[Out, Kvqe[Key, Value]#I] {

  import Monad._

  def execute[T]:Function[KeyValueQueryParameters[T, Key, Value], Out[T]] = {
    case Contains(key) =>
      point(state.get().contains(key))

    case Get(key) =>
      point(state.get().get(key))

    case Iterator() =>
      point(state.get().iterator)

    case Values() =>
      point(state.get().valuesIterator)
  }

}
