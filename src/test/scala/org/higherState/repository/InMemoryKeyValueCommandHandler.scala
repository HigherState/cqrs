package org.higherState.repository

import java.util.concurrent.atomic.AtomicReference
import org.higherState.cqrs._
import scalaz.Monad

abstract class InMemoryKeyValueCommandHandler[Out[+_]:Monad, Key, Value](state:AtomicReference[Map[Key, Value]])
  extends MonadBound[Out] with CommandHandler[Out, KeyValueCommand[Key,Value]] {

  def handle = {
    case Add(kv) =>
      state.set(state.get() + kv)
      point()
    case Remove(key) =>
      state.set(state.get() - key)
      point()
  }
}

