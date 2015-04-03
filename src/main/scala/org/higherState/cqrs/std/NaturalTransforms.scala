package org.higherState.cqrs.std

import scalaz.~>
import scala.concurrent.ExecutionContext
import org.higherState.cqrs._

trait IdentityTransforms {

  implicit def DirectPipe[T[_]] = new ~>[T, T] {
    def apply[A](fa: T[A]): T[A] = fa
  }

  implicit def IdentityValidationPipe[E] = new ~>[Id, ({type V[+T] = Valid[E,T]})#V] {
    def apply[A](value: Id[A]): Valid[E, A] =
      scalaz.Success(value)
  }

  implicit val IdentityFuturePipe = new ~>[Id, scala.concurrent.Future] {
    def apply[T](value:Id[T]): scala.concurrent.Future[T] =
      scala.concurrent.Future.successful(value)
  }

  implicit def IdentityReaderPipe[F] = new ~>[Id, ({type R[+T] = Reader[F,T]})#R] {
    def apply[T](value:Id[T]) =
      Reader(_ => value)
  }

  implicit def IdentityFutureValidPipe[E] = new ~>[Id, ({type V[+T] = FutureValid[E,T]})#V] {
    def apply[T](value: Id[T]): FutureValid[E, T] =
      scala.concurrent.Future.successful(scalaz.Success(value))
  }

  implicit def IdentityReaderValidPipe[F, E] =  new ~>[Id, ({type RV[+T] = Reader[F, Valid[E,T]]})#RV] {
     def apply[T](value:Id[T]):Reader[F, Valid[E, T]] =
       Reader(_ => scalaz.Success(value))
  }

  implicit def IdentityFutureReaderPipe[F] = new ~> [Id,({type FR[+T] = FutureReader[F, T]})#FR] {
    def apply[T](value: Id[T]): FutureReader[F, T] =
      scala.concurrent.Future.successful(Reader(_ => value))
  }

  implicit def IdentityFutureReaderValidPipe[F, E] = new ~>[Id, ({type FRV[+T] = FutureReaderValid[F, E, T]})#FRV]  {
    def apply[T](value: Id[T]):FutureReaderValid[F, E, T] =
      scala.concurrent.Future.successful(Reader(_ => scalaz.Success(value)))

  }

}

trait ValidTransforms {

  implicit def ValidationFutureValidPipe[E] = new ~>[ ({type V[+T] = Valid[E,T]})#V,  ({type V[+T] = FutureValid[E,T]})#V] {
    def apply[T](value: Valid[E, T]): FutureValid[E, T] =
      scala.concurrent.Future.successful(value)
  }

  implicit def ValidReaderValid[F,E] = new ~>[ ({type V[+T] = Valid[E,T]})#V,({type RV[+T] = ReaderValid[F, E,T]})#RV] {
    def apply[T](value: Valid[E, T]): ReaderValid[F, E,T] =
      Reader(_ => value)
  }

  implicit def ValidFutureReaderValid[F,E] = new ~>[ ({type V[+T] = Valid[E,T]})#V,({type RV[+T] = FutureReaderValid[F, E,T]})#RV] {
    def apply[T](value: Valid[E, T]): FutureReaderValid[F, E, T] =
      scala.concurrent.Future.successful(Reader(_ => value))
  }

}

trait FutureTransforms {
  import scala.concurrent.Future

//  implicit def FuturePipe[E, Out[+_]](implicit ec:ExecutionContext, pipe: ~>[Id, Out]) =
//    new ~>[Future, ({type F[+T] = Future[Out[T]]})#F] {
//      def apply[T](value:Future[T]):Future[Out[T]] =
//        value.map(t => pipe(t))
//    }


  implicit def FutureFutureValidationPipe[E](implicit ec:ExecutionContext) =
    new ~>[Future, ({type V[+T] = FutureValid[E,T]})#V] {
      def apply[T](value: Future[T]): FutureValid[E,T] =
        value.map(scalaz.Success(_))
    }

  implicit def FutureFutureReaderPipe[F](implicit ec:ExecutionContext) =
    new ~>[Future, ({type FR[+T] = FutureReader[F,T]})#FR] {
      def apply[T](value: Future[T]): FutureReader[F,T] =
        value.map(v => Reader(_ => v))
    }

  implicit def FutureFutureReaderValidPipe[F, E](implicit ec:ExecutionContext) =
    new ~>[Future,  ({type FRV[+T] = FutureReaderValid[F, E, T]})#FRV] {
      def apply[T](value:Future[T]):FutureReaderValid[F, E, T] =
        value.map(t => Reader(_ => scalaz.Success(t)))
    }

  implicit def FutureValidFutureReaderValid[F, E](implicit ec:ExecutionContext) =
    new ~>[({type V[+T] = FutureValid[E,T]})#V,  ({type FRV[+T] = FutureReaderValid[F, E, T]})#FRV] {
      def apply[T](value:FutureValid[E, T]):FutureReaderValid[F, E, T] =
        value.map(t => Reader(_ => t))
    }

  implicit def FutureReaderFutureReaderValid[F, E] (implicit ec:ExecutionContext) =
    new ~>[({type FR[+T] = FutureReader[F,T]})#FR, ({type FRV[+T] = FutureReaderValid[F, E, T]})#FRV] {
      def apply[T](value:FutureReader[F, T]):FutureReaderValid[F, E, T] =
        value.map(_.map(scalaz.Success(_)))
    }
}

trait ReaderTransforms {
  import scala.concurrent.Future

  implicit def ReaderReaderValid[F, E] = new ~>[({type R[+T] = Reader[F,T]})#R,({type RV[+T] = ReaderValid[F, E,T]})#RV] {
    def apply[T](value: Reader[F, T]):Reader[F,Valid[E,T]] =
      value.map(scalaz.Success(_))
  }

  implicit def ReaderFutureReader[F] = new ~>[({type R[+T] = Reader[F,T]})#R,({type FR[+T] = FutureReader[F, T]})#FR] {
    def apply[T](value: Reader[F, T]):Future[Reader[F,T]] =
      scala.concurrent.Future.successful(value)
  }

  implicit def ReaderFutureReaderValid[F,E] = new ~>[({type R[+T] = Reader[F,T]})#R,({type FRV[+T] = FutureReaderValid[F, E, T]})#FRV] {
    def apply[T](value: Reader[F, T]):FutureReaderValid[F,E, T] =
      scala.concurrent.Future.successful(value.map(scalaz.Success(_)))
  }

  implicit def ReaderValidFutureReaderValid[F,E] = new ~>[({type RV[+T] = ReaderValid[F, E,T]})#RV, ({type FRV[+T] = FutureReaderValid[F, E, T]})#FRV] {
    def apply[T](value: ReaderValid[F, E, T]):FutureReaderValid[F, E, T] =
      scala.concurrent.Future.successful(value)
  }
}

object NaturalTransforms extends IdentityTransforms with ValidTransforms with FutureTransforms with ReaderTransforms
