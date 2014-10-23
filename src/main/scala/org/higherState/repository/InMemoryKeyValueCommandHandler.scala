package org.higherState.repository

import java.util.concurrent.atomic.AtomicReference
import org.higherState.cqrs.{Directives, CommandHandler}

trait InMemoryKeyValueCommandHandler[Key, Value] extends CommandHandler[KeyValueCommand[Key,Value]] with Directives {
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
