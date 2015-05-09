package org.higherState.repository

import org.higherState.cqrs._
import scala.collection.mutable
import org.higherState.cqrs.std.Id

trait KeyValueRepository[Out[+_], Key, Value] extends Service[Out] {

  def contains(key:Key):Out[Boolean]

  def get(key:Key):Out[Option[Value]]

  def iterator:Out[TraversableOnce[(Key, Value)]]

  def values:Out[TraversableOnce[Value]]

  def += (kv:(Key, Value)):Out[Ack]

  def -= (key:Key):Out[Ack]
}

class KeyValueCqrsRepository[Out[+_], Key, Value](controller:CommandQueryController[Out, KeyValueCommand[Key, Value], Kvqe[Key, Value]#I]) extends KeyValueRepository[Out, Key, Value] {

  def contains(key:Key) =
    controller.executeQuery(Contains(key))

  def get(key:Key) =
    controller.executeQuery(Get(key))

  def iterator =
    controller.executeQuery(Iterator())

  def values =
    controller.executeQuery(Values())

  def += (kv:(Key, Value)) =
    controller.sendCommand(Add(kv))

  def -= (key:Key) =
    controller.sendCommand(Remove(key))
}

// simple repository for testing
class HashMapRepository[Key, Value](state:mutable.Map[Key,Value]) extends KeyValueRepository[Id, Key, Value] {
  def -=(key: Key) ={
    state -= key
    Acknowledged
  }

  def +=(kv: (Key, Value)) ={
    state += kv
    Acknowledged
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
