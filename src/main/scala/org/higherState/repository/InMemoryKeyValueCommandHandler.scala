package org.higherState.repository

import java.util.concurrent.atomic.AtomicReference
import org.higherState.cqrs._

trait InMemoryKeyValueCommandHandler[Out[+_], Key, Value] extends CommandHandler[Out, KeyValueCommand[Key,Value]] with Directives[Out] {

  protected def state:AtomicReference[Map[Key, Value]]

  def handle = {
    case Add(kv) =>
      state.set(state.get() + kv)
      complete
    case Remove(key) =>
      state.set(state.get() - key)
      complete
  }
}

