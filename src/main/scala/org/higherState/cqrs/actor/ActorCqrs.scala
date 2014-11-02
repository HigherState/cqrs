package org.higherState.cqrs.actor

import org.higherState.cqrs.{QueryExecutor, CommandHandler, Output, Cqrs}
import scala.reflect.ClassTag
import akka.actor._
import scala.concurrent.{ExecutionContext, Future}

trait ActorCqrs extends Cqrs with Output.Future {

  import akka.pattern.ask

  implicit def executionContext:ExecutionContext
  implicit def timeout:akka.util.Timeout

  def serviceName:String

  def commandHandler:ActorRef
  def queryExecutor:ActorRef

  protected def dispatchCommand(c: => C): Future[Unit] =
    commandHandler
      .ask(c)
      .mapTo[Unit]

  protected def executeQuery[T: ClassTag](qp: => QP):Future[T] =
    queryExecutor
      .ask(qp)
      .mapTo[T]

  protected def getCommandHandlerRef[T <: akka.actor.Actor with CommandHandler[C]](a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"CH-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a), s"CH-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"CH-$serviceName")
    }

  protected def getQueryRef[T <: akka.actor.Actor with QueryExecutor[QP]](a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"Q-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a), s"Q-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"Q-$serviceName")
    }

  protected def getCommandQueryRef[T <: akka.actor.Actor with QueryExecutor[QP] with CommandHandler[C]](a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"CHQ-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a), s"CHQ-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"CHQ-$serviceName")
    }
}
