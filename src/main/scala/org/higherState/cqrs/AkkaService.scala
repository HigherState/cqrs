package org.higherState.cqrs

import akka.actor._
import scala.reflect.ClassTag
import scalaz._
import scala.concurrent.{ExecutionContext, Future}

trait AkkaCqrs extends Cqrs with ActorRefBuilder with Output.Future {

  import akka.pattern.ask
  implicit def timeout:akka.util.Timeout

  protected def commandHandler:ActorRef
  protected def query:ActorRef

  protected def dispatchCommand(c: => C): Future[Unit] =
    commandHandler
      .ask(c)
      .mapTo[Unit]

  protected def executeQuery[T: ClassTag](qp: => QP):Future[T] =
    query
      .ask(qp)
      .mapTo[T]
}

trait AkkaValidationCqrs extends Cqrs with ActorRefBuilder with Output.FutureValid {

  import akka.pattern.ask
  implicit def timeout:akka.util.Timeout


  protected def commandHandler:ActorRef
  protected def query:ActorRef

  protected def dispatchCommand(c: => C): FutureValid[Unit] =
    commandHandler
      .ask(c)
      .mapTo[ValidationNel[ValidationFailure,Unit]]

  protected def executeQuery[T: ClassTag](qp: => QP):FutureValid[T] =
    query
      .ask(qp)
      .mapTo[Valid[T]]
}

trait ActorRefBuilder extends Cqrs {
  arb =>

  implicit def executionContext:ExecutionContext

  protected def getCommandHandlerRef[T <: akka.actor.Actor with CommandHandler[C]](name:String)(a: ExecutionContext => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"CH-$name")
          .getOrElse(context.actorOf(Props.apply(a(executionContext)), s"CH-$name"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a(executionContext)), s"CH-$name")
    }

  protected def getQueryRef[T <: akka.actor.Actor with Query[QP]](name:String)(a: ExecutionContext => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"Q-$name")
          .getOrElse(context.actorOf(Props.apply(a(executionContext)), s"Q-$name"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a(executionContext)), s"Q-$name")
    }
}



