package org.higherState.cqrs

import akka.actor.{ActorSelection, ActorRef}
import akka.util.Timeout
import scala.concurrent.Future

trait MessageController[Out[+_], M[_] <: Message[_]] {
  def sendMessage[T](m: => M[T]):Out[T]
}


trait CommandController[Out[+_], C <: Command] {
  def sendCommand(c: => C):Out[Ack]
}

trait QueryController[Out[+_], QP[_] <: QueryParameters[_]] {
  def executeQuery[T](qp: => QP[T]):Out[T]
}

trait MessageQueryController[Out[+_], M[_] <: Message[_], QP[_] <: QueryParameters[_]]
  extends MessageController[Out, M] with QueryController[Out, QP]

trait CommandQueryController[Out[+_], C <: Command, QP[_] <: QueryParameters[_]]
  extends CommandController[Out, C] with QueryController[Out, QP]

object MessageController {
  import akka.pattern.ask

  def apply[Out[+_], M[_] <: Message[_]](messageReceiver:MessageReceiver[Out, M]) =
    new MessageController[Out, M] {
      def sendMessage[T](m: => M[T]): Out[T] =
        messageReceiver.handle(m)
    }
  def reader[F, Out[+_], M[_] <: Message[_]](messageReceiver:MessageReceiver[({type R[+T] = Reader[F, Out[T]]})#R, M])(implicit applicator:ReaderApplicator[F]) =
    new MessageController[Out, M] {
      def sendMessage[T](m: => M[T]): Out[T] =
        applicator(messageReceiver.handle(m))
    }
  def actor[Out[+_]] = new MessageControllerCurry[Out]

  final class MessageControllerCurry[Out[+_]] {
    def apply[M[_] <: Message[_]](messageReceiver: ActorRef)(implicit timeout: Timeout):MessageController[({type F[+T] = Future[Out[T]]})#F, M] =
      new MessageController[({type F[+T] = Future[Out[T]]})#F, M] {
        def sendMessage[T](m: => M[T]): Future[Out[T]] =
          messageReceiver
            .ask(m).asInstanceOf[Future[Out[T]]]
      }
    def apply[M[_] <: Message[_]](messageReceiver: ActorSelection)(implicit timeout: Timeout):MessageController[({type F[+T] = Future[Out[T]]})#F, M] =
      new MessageController[({type F[+T] = Future[Out[T]]})#F, M] {
        def sendMessage[T](m: => M[T]): Future[Out[T]] =
          messageReceiver
            .ask(m).asInstanceOf[Future[Out[T]]]
      }

    def apply[M[_] <: Message[_]](messageReceiver:EitherActor)(implicit timeout: Timeout):MessageController[({type F[+T] = Future[Out[T]]})#F, M] =
      messageReceiver match {
        case Left(mr) => apply(mr)
        case Right(mr) => apply(mr)
      }
  }
}

object CommandController {
  import akka.pattern.ask

  def apply[Out[+_], C <: Command](commandHandler:CommandHandler[Out, C]) =
    new CommandController[Out, C] {
      def sendCommand(c: => C): Out[Ack] =
        commandHandler.handle(c)
    }
  def reader[F, Out[+_], C <: Command](commandHandler:CommandHandler[({type R[+T] = Reader[F, Out[T]]})#R, C])(implicit applicator:ReaderApplicator[F]) =
    new CommandController[Out, C] {
      def sendCommand(c: => C): Out[Ack] =
        applicator(commandHandler.handle(c))
    }
  def actor[Out[+_]] = new CommandControllerCurry[Out]

  final class CommandControllerCurry[Out[+_]] {
    def apply[C <: Command](commandHandler: ActorRef)(implicit timeout: Timeout):CommandController[({type F[+T] = Future[Out[T]]})#F, C] =
      new CommandController[({type F[+T] = Future[Out[T]]})#F, C] {
        def sendCommand(c: => C): Future[Out[Ack]] =
          commandHandler
            .ask(c).asInstanceOf[Future[Out[Ack]]]
      }

    def apply[C <: Command](commandHandler: ActorSelection)(implicit timeout: Timeout):CommandController[({type F[+T] = Future[Out[T]]})#F, C] =
      new CommandController[({type F[+T] = Future[Out[T]]})#F, C] {
        def sendCommand(c: => C): Future[Out[Ack]] =
          commandHandler
            .ask(c).asInstanceOf[Future[Out[Ack]]]
      }

    def apply[C <: Command](commandHandler:EitherActor)(implicit timeout: Timeout):CommandController[({type F[+T] = Future[Out[T]]})#F, C] =
      commandHandler match {
        case Left(mr) => apply(mr)
        case Right(mr) => apply(mr)
      }
  }
}

object QueryController {
  import akka.pattern.ask

  def apply[Out[+_], QP[_] <: QueryParameters[_]](queryExecutor:QueryExecutor[Out, QP]) =
    new QueryController[Out, QP] {
      def executeQuery[T](qp: => QP[T]): Out[T] =
        queryExecutor.execute[T](qp)
    }
  def reader[F, Out[+_], QP[_] <: QueryParameters[_]](queryExecutor:QueryExecutor[({type R[+T] = Reader[F, Out[T]]})#R, QP])(implicit applicator:ReaderApplicator[F]) =
    new QueryController[Out, QP] {
      def executeQuery[T](qp: => QP[T]): Out[T] =
        applicator(queryExecutor.execute[T](qp))
    }
  def actor[Out[+_]] = new QueryControllerCurry[Out]

  final class QueryControllerCurry[Out[+_]] {
    def apply[QP[_] <: QueryParameters[_]](queryExecutor: ActorRef)(implicit timeout: Timeout):QueryController[({type F[+T] = Future[Out[T]]})#F, QP] =
      new QueryController[({type F[+T] = Future[Out[T]]})#F, QP] {
        def executeQuery[T](qp: => QP[T]): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }
    def apply[QP[_] <: QueryParameters[_]](queryExecutor: ActorSelection)(implicit timeout: Timeout):QueryController[({type F[+T] = Future[Out[T]]})#F, QP] =
      new QueryController[({type F[+T] = Future[Out[T]]})#F, QP] {
        def executeQuery[T](qp: => QP[T]): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }

    def apply[QP[_] <: QueryParameters[_]](queryExecutor:EitherActor)(implicit timeout: Timeout):QueryController[({type F[+T] = Future[Out[T]]})#F, QP] =
      queryExecutor match {
        case Left(mr) => apply(mr)
        case Right(mr) => apply(mr)
      }
  }
}

object MessageQueryController {
  import akka.pattern.ask

  def apply[Out[+_], M[_] <: Message[_], QP[_] <: QueryParameters[_]](messageReceiver:MessageReceiver[Out, M], queryExecutor:QueryExecutor[Out, QP]) =
    new MessageQueryController[Out, M, QP] {
      def sendMessage[T](m: => M[T]): Out[T] =
        messageReceiver.handle(m)
      def executeQuery[T](qp: => QP[T]): Out[T] =
        queryExecutor.execute(qp)
    }

  def reader[F, Out[+_], M[_] <: Message[_], QP[_] <: QueryParameters[_]]
    (messageReceiver:MessageReceiver[({type R[+T] = Reader[F, Out[T]]})#R, M], queryExecutor:QueryExecutor[({type R[+T] = Reader[F, Out[T]]})#R, QP])
    (implicit applicator:ReaderApplicator[F]) =
      new MessageQueryController[Out, M, QP] {
        def sendMessage[T](m: => M[T]): Out[T] =
          applicator(messageReceiver.handle(m))
        def executeQuery[T](qp: => QP[T]): Out[T] =
          applicator(queryExecutor.execute(qp))
      }

  def partialReader[F, Out[+_],  M[_] <: Message[_], QP[_] <: QueryParameters[_]]
    (messageReceiver:MessageReceiver[({type R[+T] = Reader[F, Out[T]]})#R, M], queryExecutor:QueryExecutor[Out, QP])
    (implicit applicator:ReaderApplicator[F]) =
      new MessageQueryController[Out, M, QP] {
        def sendMessage[T](m: => M[T]): Out[T] =
          applicator(messageReceiver.handle(m))
        def executeQuery[T](qp: => QP[T]): Out[T] =
          queryExecutor.execute(qp)
      }

  def actor[Out[+_]] = new MessageQueryControllerCurry[Out]

  final class MessageQueryControllerCurry[Out[+_]] {
    def apply[M[_] <: Message[_], QP[_] <: QueryParameters[_]](messageReceiver: ActorRef, queryExecutor: ActorRef)(implicit timeout: Timeout):MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] =
      new MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] {
        def sendMessage[T](m: => M[T]): Future[Out[T]] =
          messageReceiver
            .ask(m).asInstanceOf[Future[Out[T]]]
        def executeQuery[T](qp: => QP[T]): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }
    def apply[M[_] <: Message[_], QP[_] <: QueryParameters[_]](messageReceiver: ActorSelection, queryExecutor: ActorSelection)(implicit timeout: Timeout):MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] =
      new MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] {
        def sendMessage[T](m: => M[T]): Future[Out[T]] =
          messageReceiver
            .ask(m).asInstanceOf[Future[Out[T]]]
        def executeQuery[T](qp: => QP[T]): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }

    def apply[M[_] <: Message[_], QP[_] <: QueryParameters[_]](messageReceiver:EitherActor, queryExecutor: EitherActor)(implicit timeout: Timeout):MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] =
      new MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] {
        def sendMessage[T](m: => M[T]): Future[Out[T]] =
          messageReceiver match {
            case Left(mr) =>
              mr.ask(m).asInstanceOf[Future[Out[T]]]
            case Right(mr) =>
              mr.ask(m).asInstanceOf[Future[Out[T]]]
          }


        def executeQuery[T](qp: => QP[T]): Future[Out[T]] =
          queryExecutor match {
            case Left(qe) =>
              qe.ask(qp).asInstanceOf[Future[Out[T]]]
            case Right(qe) =>
              qe.ask(qp).asInstanceOf[Future[Out[T]]]
          }
      }
  }
}

object CommandQueryController {
  import akka.pattern.ask

  def apply[Out[+_], C <: Command, QP[_] <: QueryParameters[_]](commandHandler:CommandHandler[Out, C], queryExecutor:QueryExecutor[Out, QP]) =
    new CommandQueryController[Out, C, QP] {
      def sendCommand(c: => C): Out[Ack] =
        commandHandler.handle(c)
      def executeQuery[T](qp: => QP[T]): Out[T] =
        queryExecutor.execute(qp)
    }

  def partialReader[F, Out[+_], C <: Command, QP[_] <: QueryParameters[_]]
    (commandHandler:CommandHandler[({type R[+T] = Reader[F, Out[T]]})#R, C], queryExecutor:QueryExecutor[({type R[+T] = Reader[F, Out[T]]})#R, QP])
    (implicit applicator:ReaderApplicator[F]) = {

    new CommandQueryController[Out, C, QP] {
      def sendCommand(c: => C): Out[Ack] =
        applicator(commandHandler.handle(c))
      def executeQuery[T](qp: => QP[T]): Out[T] =
        applicator(queryExecutor.execute(qp))
    }
  }

  def reader[F, Out[+_], C <: Command, QP[_] <: QueryParameters[_]]
  (commandHandler:CommandHandler[({type R[+T] = Reader[F, Out[T]]})#R, C])(queryExecutor:QueryExecutor[Out, QP])
  (implicit applicator:ReaderApplicator[F]) = {

    new CommandQueryController[Out, C, QP] {
      def sendCommand(c: => C): Out[Ack] =
        applicator(commandHandler.handle(c))
      def executeQuery[T](qp: => QP[T]): Out[T] =
        queryExecutor.execute(qp)
    }
  }

  def actor[Out[+_]] = new CommandQueryControllerCurry[Out]

  final class CommandQueryControllerCurry[Out[+_]] {

    def apply[C <: Command, QP[_] <: QueryParameters[_]](commandHandler: ActorRef, queryExecutor: ActorRef)(implicit timeout: Timeout):CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] =
      new CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] {
        def sendCommand(c: => C): Future[Out[Ack]] =
          commandHandler
            .ask(c).asInstanceOf[Future[Out[Ack]]]


        def executeQuery[T](qp: => QP[T]): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }

    def apply[C <: Command, QP[_] <: QueryParameters[_]](commandHandler: ActorSelection, queryExecutor: ActorSelection)(implicit timeout: Timeout):CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] =
      new CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] {
        def sendCommand(c: => C): Future[Out[Ack]] =
          commandHandler
            .ask(c).asInstanceOf[Future[Out[Ack]]]

        def executeQuery[T](qp: => QP[T]): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }

    def apply[C <: Command, QP[_] <: QueryParameters[_]](commandHandler: EitherActor, queryExecutor: EitherActor)(implicit timeout: Timeout):CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] =
      new CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] {
        def sendCommand(c: => C): Future[Out[Ack]] =
          commandHandler match {
            case Left(ch) =>
              ch.ask(c).asInstanceOf[Future[Out[Ack]]]
            case Right(ch) =>
              ch.ask(c).asInstanceOf[Future[Out[Ack]]]
          }


        def executeQuery[T](qp: => QP[T]): Future[Out[T]] =
          queryExecutor match {
            case Left(qe) =>
              qe.ask(qp).asInstanceOf[Future[Out[T]]]
            case Right(qe) =>
              qe.ask(qp).asInstanceOf[Future[Out[T]]]
          }
      }
    }
}



