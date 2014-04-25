package org.higherState.cqrs

import _root_.akka.actor.PoisonPill
import scala.concurrent.{ExecutionContext, Future}

trait ActorWrapper extends akka.actor.Actor {

  import _root_.akka.pattern.pipe

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
