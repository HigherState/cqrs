package org.higherState.repository

import org.higherState.cqrs.{Directives, QueryExecutor}
import java.util.concurrent.atomic.AtomicReference

trait InMemoryKeyValueQueryExecutor[Key,Value] extends QueryExecutor[KeyValueQueryParameters[Key, Value]] with Directives {

  def state:AtomicReference[Map[Key, Value]]

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
