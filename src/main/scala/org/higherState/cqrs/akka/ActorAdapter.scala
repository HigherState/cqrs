package org.higherState.cqrs.akka

import scala.concurrent.{ExecutionContext, Future}
import org.higherState.cqrs.{Output, CommandHandler, Message, Command, QueryParameters, QueryExecutor}

abstract class ActorAdapter(implicit val executionContext:ExecutionContext) extends akka.actor.Actor with Output {

  import akka.pattern.pipe

  def receive = {
    case m:Message =>
      commandQuery(m) match {
        case f:Future[_] =>
          f pipeTo sender
        case a =>
          sender ! a
      }
  }

  val commandQuery:PartialFunction[Message, Any] =
    this match {
      case cq:CommandHandler[Command@unchecked] with QueryExecutor[QueryParameters@unchecked] =>
      {
        case c:Command =>
          cq.handle(c)
        case qp:QueryParameters =>
          cq.execute(qp)
      }
      case ch:CommandHandler[Command@unchecked] =>
      {
        case c:Command =>
          ch.handle(c)
      }
      case q:QueryExecutor[QueryParameters@unchecked] =>
      {
        case qp:QueryParameters =>
          q.execute(qp)
      }
    }
}
