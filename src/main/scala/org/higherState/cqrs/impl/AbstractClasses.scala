//package org.higherState.cqrs.impl
//
//import scala.concurrent.ExecutionContext
//import akka.actor.ActorRefFactory
//
//
//abstract class ActorAdapter(implicit val executionContext:ExecutionContext) extends org.higherState.cqrs.actor.ActorAdapter
//
//abstract class ActorCqrs(val serviceName:String)(
//  implicit val executionContext:ExecutionContext,
//  val timeout:akka.util.Timeout,
//  val factory:ActorRefFactory) extends org.higherState.cqrs.actor.ActorCqrs
//
//abstract class ActorValidationCqrs(val serviceName:String)(
//  implicit val executionContext:ExecutionContext,
//  val timeout:akka.util.Timeout,
//  val factory:ActorRefFactory) extends org.higherState.cqrs.actor.ActorValidationCqrs
//
//
//abstract class FutureDirectives(implicit val executionContext:ExecutionContext) extends org.higherState.cqrs.FutureDirectives
//
//abstract class FutureValidationDirectives(implicit val executionContext:ExecutionContext) extends org.higherState.cqrs.FutureValidationDirectives
//
//abstract class FutureCqrs(implicit val executionContext:ExecutionContext) extends org.higherState.cqrs.FutureCqrs
//
//abstract class FutureValidationCqrs(implicit val executionContext:ExecutionContext) extends org.higherState.cqrs.FutureValidationCqrs
//
