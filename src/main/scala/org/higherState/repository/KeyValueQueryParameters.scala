package org.higherState.repository

import org.higherState.cqrs.QueryParameters

sealed trait KeyValueQueryParameters[Key, Value] extends QueryParameters

case class Contains[Key, Value](key:Key) extends KeyValueQueryParameters[Key, Value]

case class Get[Key, Value](key:Key) extends KeyValueQueryParameters[Key, Value]

case class Iterator[Key,Value]() extends KeyValueQueryParameters[Key, Value]

case class Values[Key,Value]() extends KeyValueQueryParameters[Key, Value]
