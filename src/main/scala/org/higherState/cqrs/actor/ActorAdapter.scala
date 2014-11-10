//package org.higherState.cqrs.actor
//
//import scala.concurrent.{ExecutionContext, Future}
//import org.higherState.cqrs._
//import akka.pattern.pipe
//
//trait ActorAdapter extends akka.actor.Actor {
//
//  implicit def executionContext:ExecutionContext
//
//  def receive = dispatcher
//
//  val dispatcher = this match {
//    case cq: CommandHandler[Command@unchecked] with QueryExecutor[QueryParameters@unchecked] =>
//      commandDispatcher(cq).orElse(queryDispatcher(cq)).andThen(pipeSender)
//    case cq: CommandHandler[Command@unchecked] with QueryExecutor[QueryParameters@unchecked]  =>
//      commandDispatcher(cq).orElse(queryDispatcher(cq)).andThen(pipeSender)
//    case ch: CommandHandler[Command@unchecked] with Output.FutureValid =>
//      commandDispatcher(ch).andThen(pipeSender)
//    case ch: CommandHandler[Command@unchecked] with Output.Future =>
//      commandDispatcher(ch).andThen(pipeSender)
//    case qe: QueryExecutor[QueryParameters@unchecked] with Output.FutureValid =>
//      queryDispatcher(qe).andThen(pipeSender)
//    case qe: QueryExecutor[QueryParameters@unchecked] with Output.Future =>
//      queryDispatcher(qe).andThen(pipeSender)
//    case ch: CommandHandler[Command@unchecked] =>
//      commandDispatcher(ch).andThen(a => sender ! a)
//    case qe: QueryExecutor[QueryParameters@unchecked] =>
//      queryDispatcher(qe).andThen(a => sender ! a)
//  }
//
//  def commandDispatcher(ch: CommandHandler[Command]): PartialFunction[Any, Any] = {
//    case c: Command =>
//      ch.handle(c)
//  }
//  def queryDispatcher(qe: QueryExecutor[QueryParameters]): PartialFunction[Any, Any] = {
//    case qp: QueryParameters =>
//      qe.execute(qp)
//  }
//
//  def pipeSender: PartialFunction[Any, Unit] = {
//    case f:Future[_] =>
//      f.pipeTo(sender)
//  }
//}