package org.higherState.cqrs

import akka.actor.{ActorSelection, ActorRef}
import akka.util.Timeout
import scala.concurrent.Future

trait MessageController[Out[+_], M <: Message] {
  def sendMessage[T](m: => M):Out[T]
}


trait CommandController[Out[+_], C <: Command] {
  def sendCommand(c: => C):Out[Unit]
}

trait QueryController[Out[+_], QP <: QueryParameters] {
  def executeQuery[T](qp: => QP):Out[T]
}

trait MessageQueryController[Out[+_], M <: Message, QP <: QueryParameters]
  extends MessageController[Out, M] with QueryController[Out, QP]

trait CommandQueryController[Out[+_], C <: Command, QP <: QueryParameters]
  extends CommandController[Out, C] with QueryController[Out, QP]

object MessageController {
  import akka.pattern.ask

  def apply[Out[+_], M <: Message](messageReceiver:MessageReceiver[Out, M]) =
    new MessageController[Out, M] {
      def sendMessage[T](m: => M): Out[T] =
        messageReceiver.handle(m).asInstanceOf[Out[T]]
    }
  def reader[F, Out[+_], M <: Message](messageReceiver:MessageReceiver[({type R[+T] = Reader[F, Out[T]]})#R, M])(implicit applicator:ReaderApplicator[F]) =
    new MessageController[Out, M] {
      def sendMessage[T](m: => M): Out[T] =
        applicator(messageReceiver.handle(m)).asInstanceOf[Out[T]]
    }
  def actor[Out[+_]] = new {
    def apply[M <: Message](messageReceiver: ActorRef)(implicit timeout: Timeout):MessageController[({type F[+T] = Future[Out[T]]})#F, M] =
      new MessageController[({type F[+T] = Future[Out[T]]})#F, M] {
        def sendMessage[T](m: => M): Future[Out[T]] =
          messageReceiver
            .ask(m).asInstanceOf[Future[Out[T]]]
      }
    def apply[M <: Message](messageReceiver: ActorSelection)(implicit timeout: Timeout):MessageController[({type F[+T] = Future[Out[T]]})#F, M] =
      new MessageController[({type F[+T] = Future[Out[T]]})#F, M] {
        def sendMessage[T](m: => M): Future[Out[T]] =
          messageReceiver
            .ask(m).asInstanceOf[Future[Out[T]]]
      }

    def apply[M <: Message](messageReceiver:EitherActor)(implicit timeout: Timeout):MessageController[({type F[+T] = Future[Out[T]]})#F, M] =
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
      def sendCommand(c: => C): Out[Unit] =
        commandHandler.handle(c)
    }
  def reader[F, Out[+_], C <: Command](commandHandler:CommandHandler[({type R[+T] = Reader[F, Out[T]]})#R, C])(implicit applicator:ReaderApplicator[F]) =
    new CommandController[Out, C] {
      def sendCommand(c: => C): Out[Unit] =
        applicator(commandHandler.handle(c))
    }
  def actor[Out[+_]] = new {
    def apply[C <: Command](commandHandler: ActorRef)(implicit timeout: Timeout):CommandController[({type F[+T] = Future[Out[T]]})#F, C] =
      new CommandController[({type F[+T] = Future[Out[T]]})#F, C] {
        def sendCommand(c: => C): Future[Out[Unit]] =
          commandHandler
            .ask(c).asInstanceOf[Future[Out[Unit]]]
      }

    def apply[C <: Command](commandHandler: ActorSelection)(implicit timeout: Timeout):CommandController[({type F[+T] = Future[Out[T]]})#F, C] =
      new CommandController[({type F[+T] = Future[Out[T]]})#F, C] {
        def sendCommand(c: => C): Future[Out[Unit]] =
          commandHandler
            .ask(c).asInstanceOf[Future[Out[Unit]]]
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

  def apply[Out[+_], QP <: QueryParameters](queryExecutor:QueryExecutor[Out, QP]) =
    new QueryController[Out, QP] {
      def executeQuery[T](qp: => QP): Out[T] =
        queryExecutor.execute(qp).asInstanceOf[Out[T]]
    }
  def reader[F, Out[+_], QP <: QueryParameters](queryExecutor:QueryExecutor[({type R[+T] = Reader[F, Out[T]]})#R, QP])(implicit applicator:ReaderApplicator[F]) =
    new QueryController[Out, QP] {
      def executeQuery[T](qp: => QP): Out[T] =
        applicator(queryExecutor.execute(qp)).asInstanceOf[Out[T]]
    }
  def actor[Out[+_]] = new {
    def apply[QP <: QueryParameters](queryExecutor: ActorRef)(implicit timeout: Timeout):QueryController[({type F[+T] = Future[Out[T]]})#F, QP] =
      new QueryController[({type F[+T] = Future[Out[T]]})#F, QP] {
        def executeQuery[T](qp: => QP): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }
    def apply[QP <: QueryParameters](queryExecutor: ActorSelection)(implicit timeout: Timeout):QueryController[({type F[+T] = Future[Out[T]]})#F, QP] =
      new QueryController[({type F[+T] = Future[Out[T]]})#F, QP] {
        def executeQuery[T](qp: => QP): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }

    def apply[QP <: QueryParameters](queryExecutor:EitherActor)(implicit timeout: Timeout):QueryController[({type F[+T] = Future[Out[T]]})#F, QP] =
      queryExecutor match {
        case Left(mr) => apply(mr)
        case Right(mr) => apply(mr)
      }
  }
}

object MessageQueryController {
  import akka.pattern.ask

  def apply[Out[+_], M <: Message, QP <: QueryParameters](messageReceiver:MessageReceiver[Out, M], queryExecutor:QueryExecutor[Out, QP]) =
    new MessageQueryController[Out, M, QP] {
      def sendMessage[T](m: => M): Out[T] =
        messageReceiver.handle(m).asInstanceOf[Out[T]]
      def executeQuery[T](qp: => QP): Out[T] =
        queryExecutor.execute(qp).asInstanceOf[Out[T]]
    }

  def reader[F, Out[+_], M <: Message, QP <: QueryParameters]
    (messageReceiver:MessageReceiver[({type R[+T] = Reader[F, Out[T]]})#R, M], queryExecutor:QueryExecutor[({type R[+T] = Reader[F, Out[T]]})#R, QP])
    (implicit applicator:ReaderApplicator[F]) =
      new MessageQueryController[Out, M, QP] {
        def sendMessage[T](m: => M): Out[T] =
          applicator(messageReceiver.handle(m)).asInstanceOf[Out[T]]
        def executeQuery[T](qp: => QP): Out[T] =
          applicator(queryExecutor.execute(qp)).asInstanceOf[Out[T]]
      }

  def partialReader[F, Out[+_],  M <: Message, QP <: QueryParameters]
    (messageReceiver:MessageReceiver[({type R[+T] = Reader[F, Out[T]]})#R, M], queryExecutor:QueryExecutor[Out, QP])
    (implicit applicator:ReaderApplicator[F]) =
      new MessageQueryController[Out, M, QP] {
        def sendMessage[T](m: => M): Out[T] =
          applicator(messageReceiver.handle(m)).asInstanceOf[Out[T]]
        def executeQuery[T](qp: => QP): Out[T] =
          queryExecutor.execute(qp).asInstanceOf[Out[T]]
      }

  def actor[Out[+_]] = new {
    def apply[M <: Message, QP <: QueryParameters](messageReceiver: ActorRef, queryExecutor: ActorRef)(implicit timeout: Timeout):MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] =
      new MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] {
        def sendMessage[T](m: => M): Future[Out[T]] =
          messageReceiver
            .ask(m).asInstanceOf[Future[Out[T]]]
        def executeQuery[T](qp: => QP): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }
    def apply[M <: Message, QP <: QueryParameters](messageReceiver: ActorSelection, queryExecutor: ActorSelection)(implicit timeout: Timeout):MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] =
      new MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] {
        def sendMessage[T](m: => M): Future[Out[T]] =
          messageReceiver
            .ask(m).asInstanceOf[Future[Out[T]]]
        def executeQuery[T](qp: => QP): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }

    def apply[M <: Message, QP <: QueryParameters](messageReceiver:EitherActor, queryExecutor: EitherActor)(implicit timeout: Timeout):MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] =
      new MessageQueryController[({type F[+T] = Future[Out[T]]})#F, M, QP] {
        def sendMessage[T](m: => M): Future[Out[T]] =
          messageReceiver match {
            case Left(mr) =>
              mr.ask(m).asInstanceOf[Future[Out[T]]]
            case Right(mr) =>
              mr.ask(m).asInstanceOf[Future[Out[T]]]
          }


        def executeQuery[T](qp: => QP): Future[Out[T]] =
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

  def apply[Out[+_], C <: Command, QP <: QueryParameters](commandHandler:CommandHandler[Out, C], queryExecutor:QueryExecutor[Out, QP]) =
    new CommandQueryController[Out, C, QP] {
      def sendCommand(c: => C): Out[Unit] =
        commandHandler.handle(c)
      def executeQuery[T](qp: => QP): Out[T] =
        queryExecutor.execute(qp).asInstanceOf[Out[T]]
    }

  def partialReader[F, Out[+_], C <: Command, QP <: QueryParameters]
    (commandHandler:CommandHandler[({type R[+T] = Reader[F, Out[T]]})#R, C], queryExecutor:QueryExecutor[({type R[+T] = Reader[F, Out[T]]})#R, QP])
    (implicit applicator:ReaderApplicator[F]) = {

    new CommandQueryController[Out, C, QP] {
      def sendCommand(c: => C): Out[Unit] =
        applicator(commandHandler.handle(c))
      def executeQuery[T](qp: => QP): Out[T] =
        applicator(queryExecutor.execute(qp)).asInstanceOf[Out[T]]
    }
  }

  def reader[F, Out[+_], C <: Command, QP <: QueryParameters]
  (commandHandler:CommandHandler[({type R[+T] = Reader[F, Out[T]]})#R, C])(queryExecutor:QueryExecutor[Out, QP])
  (implicit applicator:ReaderApplicator[F]) = {

    new CommandQueryController[Out, C, QP] {
      def sendCommand(c: => C): Out[Unit] =
        applicator(commandHandler.handle(c))
      def executeQuery[T](qp: => QP): Out[T] =
        queryExecutor.execute(qp).asInstanceOf[Out[T]]
    }
  }

  def actor[Out[+_]] = new {
    def apply[C <: Command, QP <: QueryParameters](commandHandler: ActorRef, queryExecutor: ActorRef)(implicit timeout: Timeout):CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] =
      new CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] {
        def sendCommand(c: => C): Future[Out[Unit]] =
          commandHandler
            .ask(c).asInstanceOf[Future[Out[Unit]]]


        def executeQuery[T](qp: => QP): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }

    def apply[C <: Command, QP <: QueryParameters](commandHandler: ActorSelection, queryExecutor: ActorSelection)(implicit timeout: Timeout):CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] =
      new CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] {
        def sendCommand(c: => C): Future[Out[Unit]] =
          commandHandler
            .ask(c).asInstanceOf[Future[Out[Unit]]]

        def executeQuery[T](qp: => QP): Future[Out[T]] =
          queryExecutor
            .ask(qp).asInstanceOf[Future[Out[T]]]
      }

    def apply[C <: Command, QP <: QueryParameters](commandHandler: EitherActor, queryExecutor: EitherActor)(implicit timeout: Timeout):CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] =
      new CommandQueryController[({type F[+T] = Future[Out[T]]})#F, C, QP] {
        def sendCommand(c: => C): Future[Out[Unit]] =
          commandHandler match {
            case Left(ch) =>
              ch.ask(c).asInstanceOf[Future[Out[Unit]]]
            case Right(ch) =>
              ch.ask(c).asInstanceOf[Future[Out[Unit]]]
          }


        def executeQuery[T](qp: => QP): Future[Out[T]] =
          queryExecutor match {
            case Left(qe) =>
              qe.ask(qp).asInstanceOf[Future[Out[T]]]
            case Right(qe) =>
              qe.ask(qp).asInstanceOf[Future[Out[T]]]
          }
      }
  }
}



