package org.higherState.cqrs.actor

import scala.concurrent.{ExecutionContext, Future}
import org.higherState.cqrs._

abstract class ActorAdapter(implicit val executionContext:ExecutionContext) extends akka.actor.Actor {

  import akka.pattern.pipe

  def receive = dispatcher

  val dispatcher = this match {
    case cq: CommandHandler[Command@unchecked] with QueryExecutor[QueryParameters@unchecked] with Output.Future =>
      commandDispatcher(cq).orElse(queryDispatcher(cq)).andThen(pipe)
    case ch: CommandHandler[Command@unchecked] with Output.Future =>
      commandDispatcher(ch).andThen(pipe)
    case qe: QueryExecutor[QueryParameters@unchecked] with Output.Future =>
      queryDispatcher(qe).andThen(a => sender ! a)
    case ch: CommandHandler[Command@unchecked] =>
      commandDispatcher(ch).andThen(a => sender ! a)
    case qe: QueryExecutor[QueryParameters@unchecked] =>
      queryDispatcher(qe).andThen(a => sender ! a)
  }

  def commandDispatcher(ch: CommandHandler[Command]): PartialFunction[Any, Any] = {
    case c: Command =>
      ch.handle(c)
  }
  def queryDispatcher(qe: QueryExecutor[QueryParameters]): PartialFunction[Any, Any] = {
    case qp: QueryParameters =>
      qe.execute(qp)
  }
  def pipe: PartialFunction[Any, Unit] = {
    case f:Future[_] => f pipeTo sender
  }
}