package org.higherState.repository

import org.higherState.cqrs.{Output, Service, CqrsService, Iter}
import scala.collection.mutable

trait KeyValueRepository[Key, Value] extends Service {

  def contains(key:Key):Out[Boolean]

  def get(key:Key):Out[Option[Value]]

  def iterator:Out[Iter[(Key, Value)]]

  def values:Out[Iter[Value]]

  def += (kv:(Key, Value)):Out[Unit]

  def -= (key:Key):Out[Unit]

}

trait KeyValueCqrsRepository[Key, Value] extends CqrsService[KeyValueCommand[Key, Value], KeyValueQueryParameters[Key,Value]] with KeyValueRepository[Key, Value] {

  def contains(key:Key):Out[Boolean] =
    executeQuery[Boolean](Contains(key))

  def get(key:Key):Out[Option[Value]] =
    executeQuery[Option[Value]](Get(key))

  def iterator:Out[Iter[(Key, Value)]] =
    executeQuery[Iter[(Key, Value)]](Iterator())

  def values:Out[Iter[Value]] =
    executeQuery[Iter[Value]](Values())

  def += (kv:(Key, Value)):Out[Unit] =
    dispatchCommand(Add(kv))

  def -= (key:Key):Out[Unit] =
    dispatchCommand(Remove(key))
}

// simple repository for testing
case class HashMapRepository[Key, Value](state:mutable.Map[Key,Value]) extends KeyValueRepository[Key, Value] with Output.Identity {
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
