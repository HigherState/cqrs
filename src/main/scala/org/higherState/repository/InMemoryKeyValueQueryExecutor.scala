package org.higherState.repository

import org.higherState.cqrs.{Directives, QueryExecutor}
import java.util.concurrent.atomic.AtomicReference
import scalaz.Monad

trait InMemoryKeyValueQueryExecutor[Out[+_], Key, Value]extends QueryExecutor[Out, KeyValueQueryParameters[Key, Value]] with Directives[Out] {

  protected def state:AtomicReference[Map[Key, Value]]

  def execute = {
    case Contains(key) =>
      unit(state.get().contains(key))

    case Get(key) =>
      unit(state.get().get(key))

    case Iterator() =>
      unit(state.get().iterator)

    case Values() =>
      unit(state.get().valuesIterator)
  }
}
