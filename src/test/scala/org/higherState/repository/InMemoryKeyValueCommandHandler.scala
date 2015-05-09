package org.higherState.repository

import java.util.concurrent.atomic.AtomicReference
import org.higherState.cqrs._

class InMemoryKeyValueCommandHandler[Out[+_]:Monad, Key, Value](state:AtomicReference[Map[Key, Value]])
  extends CommandHandler[Out, KeyValueCommand[Key,Value]] {

  import Monad._

  def handle = {
    case Add(kv) =>
      state.set(state.get() + kv)
      acknowledged
    case Remove(key) =>
      state.set(state.get() - key)
      acknowledged
  }
}

