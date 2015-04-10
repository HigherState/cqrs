package org.higherState.cqrs

import scala.concurrent.Future
import akka.pattern.PipeableFuture

trait ActorAdapter extends akka.actor.Actor {

  def receive = handler

  //def applicator(implicit readerApplicator:ReaderApplicator)

  val handler = this match {
    case cq: CommandHandler[_, Command@unchecked] with QueryExecutor[_, QueryParameters@unchecked] =>
      handleCommand(cq).orElse(executeQuery(cq)).andThen(pipeSender)
    case ch: CommandHandler[_, Command@unchecked] =>
      handleCommand(ch).andThen(pipeSender)
    case qe: QueryExecutor[_, QueryParameters@unchecked] =>
      executeQuery(qe).andThen(pipeSender)
    case m: MessageReceiver[_, Message@unchecked] =>
      sendMessage(m).andThen(pipeSender)
  }

  protected def handleCommand[Out[+_]](ch: CommandHandler[Out, Command]): PartialFunction[Any, Any] = {
    case c: Command =>
      ch.handle(c)
  }

  protected def executeQuery[Out[+_]](qe: QueryExecutor[Out, QueryParameters]): PartialFunction[Any, Any] = {
    case qp: QueryParameters =>
      qe.execute(qp)
  }

  protected def sendMessage[Out[+_]](mr: MessageReceiver[Out, Message]): PartialFunction[Any, Any] = {
    case m:Message =>
      mr.handle(m)
  }

  private def pipeSender: PartialFunction[Any, Unit] = {
    case f:Future[_] =>
      new PipeableFuture(f)(this.context.dispatcher) pipeTo sender
    case a =>
      sender ! a
  }
}