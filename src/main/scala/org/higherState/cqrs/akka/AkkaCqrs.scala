package org.higherState.cqrs.akka

import org.higherState.cqrs.{QueryExecutor, CommandHandler, Output, Cqrs}
import scala.reflect.ClassTag
import akka.actor._
import scala.concurrent.{ExecutionContext, Future}

abstract class AkkaCqrs(implicit val executionContext:ExecutionContext, timeout:akka.util.Timeout) extends Cqrs with Output.Future {

  import akka.pattern.ask

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

  protected def getCommandHandlerRef[T <: akka.actor.Actor with CommandHandler[C]](serviceName:String)(a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"CH-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a), s"CH-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"CH-$serviceName")
    }

  protected def getQueryRef[T <: akka.actor.Actor with QueryExecutor[QP]](serviceName:String)(a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"Q-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a), s"Q-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"Q-$serviceName")
    }

  protected def getCommandQueryRef[T <: akka.actor.Actor with QueryExecutor[QP] with CommandHandler[C]](serviceName:String)(a: => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"CHQ-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a), s"CHQ-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a), s"CHQ-$serviceName")
    }
}
