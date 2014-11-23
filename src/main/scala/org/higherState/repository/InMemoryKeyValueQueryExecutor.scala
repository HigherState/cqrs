package org.higherState.repository

import org.higherState.cqrs.QueryExecutor
import java.util.concurrent.atomic.AtomicReference
import scalaz.Monad

trait InMemoryKeyValueQueryExecutor[Out[+_], Key, Value]extends QueryExecutor[Out, KeyValueQueryParameters[Key, Value]] with Monad[Out] {

  protected def state:AtomicReference[Map[Key, Value]]

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
