package org.higherState.repository

import org.higherState.cqrs.{MonadBound, QueryExecutor}
import java.util.concurrent.atomic.AtomicReference
import scalaz.Monad

abstract class InMemoryKeyValueQueryExecutor[Out[+_]:Monad, Key, Value](state:AtomicReference[Map[Key, Value]])
  extends MonadBound[Out] with QueryExecutor[Out, KeyValueQueryParameters[Key, Value]] {

  def execute = {
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
