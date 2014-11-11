package org.higherState.cqrs.akkaPattern

import scala.concurrent.{ExecutionContext, Future}
import org.higherState.cqrs._
import akka.pattern.pipe

trait ActorAdapter extends akka.actor.Actor {

  implicit def executionContext:ExecutionContext

  def receive = handler

  val handler = this match {
    case cq: CommandHandler[_, Command@unchecked] with QueryExecutor[_, QueryParameters@unchecked] =>
      handleCommand(cq).orElse(executeQuery(cq)).andThen(pipeSender)
    case ch: CommandHandler[_, Command@unchecked] =>
      handleCommand(ch).andThen(pipeSender)
    case qe: QueryExecutor[_, QueryParameters@unchecked] =>
      executeQuery(qe).andThen(pipeSender)
  }

  private def handleCommand[Out[+_]](ch: CommandHandler[Out, Command]): PartialFunction[Any, Any] = {
    case c: Command =>
      ch.handle(c)
  }

  private def executeQuery[Out[+_]](qe: QueryExecutor[Out, QueryParameters]): PartialFunction[Any, Any] = {
    case qp: QueryParameters =>
      qe.execute(qp)
  }

  private def pipeSender: PartialFunction[Any, Unit] = {
    case f:Future[_] =>
      f pipeTo sender
    case a =>
      sender ! a
  }
}