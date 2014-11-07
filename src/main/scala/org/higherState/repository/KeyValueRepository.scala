package org.higherState.repository

import org.higherState.cqrs.{Output, CqrsService, Iter}
import scala.collection.mutable
import org.higherState.cqrs2

trait KeyValueRepository[Out[+_], Key, Value] extends cqrs2.Service[Out] {

  def contains(key:Key):Out[Boolean]

  def get(key:Key):Out[Option[Value]]

  def iterator:Out[Iter[(Key, Value)]]

  def values:Out[Iter[Value]]

  def += (kv:(Key, Value)):Out[Unit]

  def -= (key:Key):Out[Unit]

}

trait KeyValueCqrsRepository[Out[+_], Key, Value] extends cqrs2.CqrsService[Out, KeyValueCommand[Key, Value], KeyValueQueryParameters[Key,Value]] with KeyValueRepository[Out, Key, Value] {

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
case class HashMapRepository[Key, Value](state:mutable.Map[Key,Value]) extends KeyValueRepository[cqrs2.Id, Key, Value] {
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
