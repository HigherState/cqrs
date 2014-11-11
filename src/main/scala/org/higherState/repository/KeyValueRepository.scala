package org.higherState.repository

import org.higherState.cqrs._
import scala.collection.mutable

trait KeyValueRepository[Out[+_], Key, Value] extends Service[Out] {

  def contains(key:Key):Out[Boolean]

  def get(key:Key):Out[Option[Value]]

  def iterator:Out[Iter[(Key, Value)]]

  def values:Out[Iter[Value]]

  def += (kv:(Key, Value)):Out[Unit]

  def -= (key:Key):Out[Unit]
}

class KeyValueCqrsRepository[Out[+_], Key, Value](dispatcher:Dispatcher[Out, KeyValueCommand[Key, Value], KeyValueQueryParameters[Key, Value]]) extends KeyValueRepository[Out, Key, Value] {

  def contains(key:Key):Out[Boolean] =
    dispatcher.executeQuery[Boolean](Contains(key))

  def get(key:Key):Out[Option[Value]] =
    dispatcher.executeQuery[Option[Value]](Get(key))

  def iterator:Out[Iter[(Key, Value)]] =
    dispatcher.executeQuery[Iter[(Key, Value)]](Iterator())

  def values:Out[Iter[Value]] =
    dispatcher.executeQuery[Iter[Value]](Values())

  def += (kv:(Key, Value)):Out[Unit] =
    dispatcher.sendCommand(Add(kv))

  def -= (key:Key):Out[Unit] =
    dispatcher.sendCommand(Remove(key))
}

// simple repository for testing
class HashMapRepository[Key, Value](state:mutable.Map[Key,Value]) extends KeyValueRepository[Identity, Key, Value] {
  def -=(key: Key) {
    state -= key
  }

  def +=(kv: (Key, Value)) {
    state += kv
  }

  def values =
    state.values

  def iterator =
    state.iterator

  def get(key: Key) =
    state.get(key)

  def contains(key: Key) =
    state.contains(key)
}
