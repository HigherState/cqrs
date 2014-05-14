package org.higherState.cqrs

import scala.concurrent.{ExecutionContext, Future}

//requires executionContext even if CQ does return futures
//Unable to get =:!= to check on mixed in abstract types
trait ActorAdapter extends akka.actor.Actor with Output {

  import akka.pattern.pipe

  implicit def executionContext:ExecutionContext

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
      case cq:CommandHandler[Command] with Query[QueryParameters] =>
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
      case q:Query[QueryParameters@unchecked] =>
      {
        case qp:QueryParameters =>
          q.execute(qp)
      }
    }
}
