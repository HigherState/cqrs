package org.higherState.repository

import org.higherState.cqrs._
import scala.collection.mutable
import org.higherState.cqrs.std.Id

trait KeyValueRepository[Out[+_], Key, Value] extends Service[Out] {

  def contains(key:Key):Out[Boolean]

  def get(key:Key):Out[Option[Value]]

  def iterator:Out[TraversableOnce[(Key, Value)]]

  def values:Out[TraversableOnce[Value]]

  def += (kv:(Key, Value)):Out[Unit]

  def -= (key:Key):Out[Unit]
}

class KeyValueCqrsRepository[Out[+_], Key, Value](controller:CommandQueryController[Out, KeyValueCommand[Key, Value], KeyValueQueryParameters[Key, Value]]) extends KeyValueRepository[Out, Key, Value] {

  def contains(key:Key):Out[Boolean] =
    controller.executeQuery[Boolean](Contains(key))

  def get(key:Key):Out[Option[Value]] =
    controller.executeQuery[Option[Value]](Get(key))

  def iterator:Out[TraversableOnce[(Key, Value)]] =
    controller.executeQuery[TraversableOnce[(Key, Value)]](Iterator())

  def values:Out[TraversableOnce[Value]] =
    controller.executeQuery[TraversableOnce[Value]](Values())

  def += (kv:(Key, Value)):Out[Unit] =
    controller.sendCommand(Add(kv))

  def -= (key:Key):Out[Unit] =
    controller.sendCommand(Remove(key))
}

// simple repository for testing
class HashMapRepository[Key, Value](state:mutable.Map[Key,Value]) extends KeyValueRepository[Id, Key, Value] {
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
