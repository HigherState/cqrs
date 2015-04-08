package org.higherState.repository

import org.higherState.cqrs.Command

sealed trait KeyValueCommand[Key, Value] extends Command

case class Add[Key, Value](kv:(Key, Value)) extends KeyValueCommand[Key, Value]

case class Remove[Key, Value](key:Key) extends KeyValueCommand[Key, Value]

