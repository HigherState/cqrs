package org.higherState.cqrs.akka

import akka.actor.PoisonPill
import org.higherState.cqrs.{Query, CommandHandler, Message}
import scala.concurrent.{ExecutionContext, Future}

trait ActorWrapper extends akka.actor.Actor {

  import akka.pattern.pipe

  implicit def executionContext:ExecutionContext

  def kill() {
    self ! PoisonPill
  }

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
      case cq:CommandHandler with Query =>
      {
        case c:cq.C =>
          cq.handle(c)
        case qp:cq.QP =>
          cq.execute(qp)
      }
      case ch:CommandHandler =>
      {
        case c:ch.C =>
          ch.handle(c)
      }
      case q:Query =>
      {
        case qp:q.QP =>
          q.execute(qp)
      }

    }

//  val validatorFunction:PartialFunction[Message, Valid[_]] =
//    this match {
//      case v:Validator =>
//      {
//        case m:Message =>
//          v.validate.lift(m).collect {
//            case head :: tail =>
//              Failure(NonEmptyList(head, tail.toSeq:_*))
//          }.getOrElse(commandQuery(m))
//      }
//      case _ =>
//        commandQuery
//    }

//  val logger:PartialFunction[Message, Valid[_]] =
//    this match {
//      case l:Logger => {
//        case m:Message =>
//          l.log.apply(m)
//          validatorFunction(m)
//      }
//      case _ =>
//        validatorFunction
//    }

//  val handleMessage:PartialFunction[Message, Unit] =
//    logger
//      .andThen(v => sender ! v)
//      .orElse {
//      case m => throw UnexpectedMessageException(m)
//    }

}
