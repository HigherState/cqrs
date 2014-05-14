package org.higherState.cqrs

import scala.reflect.ClassTag
import akka.actor._
import scala.concurrent.{ExecutionContext, Future}
import scalaz._

trait Service extends Output

sealed trait Cqrs extends Service {

  type C <: Command
  type QP <: QueryParameters

  protected def dispatchCommand(c: => C):Out[Unit]

  protected def executeQuery[T:ClassTag](qp: => QP):Out[T]

}

trait CqrsService[_C <: Command, _QP <: QueryParameters] extends Cqrs {

  type C = _C
  type QP = _QP

}

trait IdentityCqrs extends Cqrs with Output.Identity {

  def commandHandler:CommandHandler[C] with Output.Identity

  def query:Query[QP] with Output.Identity

  protected def dispatchCommand(c: => C): Unit = {
    commandHandler.handle(c)
  }

  protected def executeQuery[T: ClassTag](qp: => QP): T = {
    query.execute(qp).asInstanceOf[T]
  }
}

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

  protected def getCommandHandlerRef[T <: akka.actor.Actor with CommandHandler[C]](serviceName:String)(a: ExecutionContext => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"CH-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a(executionContext)), s"CH-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a(executionContext)), s"CH-$serviceName")
    }

  protected def getQueryRef[T <: akka.actor.Actor with Query[QP]](serviceName:String)(a: ExecutionContext => T)(implicit factory:ActorRefFactory, t:ClassTag[T]) =
    factory match {
      case context:ActorContext =>
        context
          .child(s"Q-$serviceName")
          .getOrElse(context.actorOf(Props.apply(a(executionContext)), s"Q-$serviceName"))
      case system:ActorSystem =>
        system.actorOf(Props.apply(a(executionContext)), s"Q-$serviceName")
    }
}





