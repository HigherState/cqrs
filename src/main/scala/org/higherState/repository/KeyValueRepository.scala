package org.higherState.repository

import org.higherState.cqrs.{Service, CqrsService, Iter}


trait KeyValueRepository[Key, Value] extends Service {

  def contains(key:Key):Out[Boolean]

  def get(key:Key):Out[Option[Value]]

  def iterator:Out[Iter[(Key, Value)]]

  def values:Out[Iter[Value]]

  def += (kv:(Key, Value)):Out[Unit]

  def -= (key:Key):Out[Unit]

}


trait KeyValueCqrsRepository[Key, Value] extends CqrsService[KeyValueCommand[Key, Value], KeyValueQueryParameters[Key,Value]] with KeyValueRepository {

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
