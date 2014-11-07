package org.higherState.repository

import org.higherState.cqrs.{Directives, QueryExecutor}
import java.util.concurrent.atomic.AtomicReference
import scalaz.Monad
import org.higherState.cqrs2

abstract class InMemoryKeyValueQueryExecutor[Out[+_], Key,Value](implicit fm:Monad[Out]) extends cqrs2.QueryExecutor[Out, KeyValueQueryParameters[Key, Value]] with cqrs2.Directives[Out] {

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
