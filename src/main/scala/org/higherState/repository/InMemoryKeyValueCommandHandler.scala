package org.higherState.repository

import java.util.concurrent.atomic.AtomicReference
import scalaz.Monad
import org.higherState.cqrs._

abstract class InMemoryKeyValueCommandHandler[Out[+_], Key, Value](implicit val fm:Monad[Out]) extends CommandHandler[Out, KeyValueCommand[Key,Value]] with Directives[Out] {
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

