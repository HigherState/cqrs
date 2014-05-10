package org.higherState.cqrs

import scala.concurrent.{ExecutionContext, Future}
import shapeless.TypeOperators._


//requires executionContext even if CQ does return futures
//Could look at <:!<  in shapeless to insure
trait ActorWrapper extends akka.actor.Actor {

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
      case ch:CommandHandler[Command] =>
      {
        case c:Command =>
          ch.handle(c)
      }
      case q:Query[QueryParameters] =>
      {
        case qp:QueryParameters =>
          q.execute(qp)
      }

    }
}
