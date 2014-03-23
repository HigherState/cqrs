package org.higherState.cqrs.akka

import org.higherState.cqrs.{QueryParameters, Command, Query, CommandHandler}
import akka.actor.{ActorSystem, Props, ActorContext, ActorRefFactory}
import scala.reflect.ClassTag

trait ActorRefBuilder {
  arb =>

  type C <: Command
  type QP <: QueryParameters

  protected def getCommandHandlerRef[T <: akka.actor.Actor with CommandHandler{ type C = arb.C}](name:String)(a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"CH-$name")
          .getOrElse(context.actorOf(Props.apply(a), s"CH-$name"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"CH-$name")
    }

  protected def getQueryRef[T <: akka.actor.Actor with Query{ type QP = arb.QP}](name:String)(a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"Q-$name")
          .getOrElse(context.actorOf(Props.apply(a), s"Q-$name"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"Q-$name")
    }
}
