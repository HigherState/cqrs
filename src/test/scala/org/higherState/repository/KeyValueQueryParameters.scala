package org.higherState.repository

import org.higherState.cqrs.QueryParameters

sealed trait KeyValueQueryParameters[R, Key, Value] extends QueryParameters[R]

final case class Contains[Key, Value](key:Key) extends KeyValueQueryParameters[Boolean, Key, Value]

final case class Get[Key, Value](key:Key) extends KeyValueQueryParameters[Option[Value], Key, Value]

final case class Iterator[Key,Value]() extends KeyValueQueryParameters[TraversableOnce[(Key, Value)], Key, Value]

final case class Values[Key,Value]() extends KeyValueQueryParameters[TraversableOnce[Value], Key, Value]
