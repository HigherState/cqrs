package org.higherState.cqrs.pipes

import org.higherState.cqrs.QueryParameters

sealed trait MapQueryParameters extends QueryParameters

case class Get(key:Int) extends MapQueryParameters

case object Values extends MapQueryParameters