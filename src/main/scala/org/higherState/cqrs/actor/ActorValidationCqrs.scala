package org.higherState.cqrs.actor

import scala.reflect.ClassTag
import akka.actor._

import org.higherState.cqrs.{QueryExecutor, CommandHandler, Output, Cqrs, FutureValid, Valid, FailureDirectives}

abstract class ActorValidationCqrs(implicit timeout:akka.util.Timeout) extends Cqrs with Output.FutureValid {

  import akka.pattern.ask

  def commandHandler:ActorRef
  def queryExecutor:ActorRef

  protected def dispatchCommand(c: => C): FutureValid[Unit] =
    commandHandler
      .ask(c)
      .mapTo[Valid[Unit]]


  protected def executeQuery[T: ClassTag](qp: => QP):FutureValid[T] =
    queryExecutor
      .ask(qp)
      .mapTo[Valid[T]]

  protected def getCommandHandlerRef[T <: akka.actor.Actor with CommandHandler[C] with FailureDirectives](serviceName:String)(a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"CH-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a), s"CH-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"CH-$serviceName")
    }

  protected def getQueryRef[T <: akka.actor.Actor with QueryExecutor[QP] with FailureDirectives](serviceName:String)(a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"Q-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a), s"Q-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"Q-$serviceName")
    }

  protected def getCommandQueryRef[T <: akka.actor.Actor with QueryExecutor[QP] with CommandHandler[C] with FailureDirectives](serviceName:String)(a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"CHQ-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a), s"CHQ-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"CHQ-$serviceName")
    }
}
