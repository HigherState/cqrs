package org.higherState.repository

import java.util.concurrent.atomic.AtomicReference
import org.higherState.cqrs2
import scalaz.Monad

abstract class InMemoryKeyValueCommandHandler[Out[+_], Key, Value](implicit val fm:Monad[Out]) extends cqrs2.CommandHandler[Out, KeyValueCommand[Key,Value]] with cqrs2.Directives[Out] {
  def state:AtomicReference[Map[Key, Value]]

  def handle = {
    case Add(kv) =>
      state.set(state.get() + kv)
      complete
    case Remove(key) =>
      state.set(state.get() - key)
      complete
  }
}

