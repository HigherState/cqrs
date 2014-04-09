package org.higherState.cqrs.pipes

import org.higherState.cqrs.Command

sealed trait MapCommand extends Command
case class Put(key:Int, value:String) extends MapCommand


