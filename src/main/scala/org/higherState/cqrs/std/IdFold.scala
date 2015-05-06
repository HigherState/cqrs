package org.higherState.cqrs.std

import org.higherState.cqrs.Fold
import scala.concurrent.{Future, ExecutionContext}

trait IdFold {

  implicit val idFold:Fold[Id] =
    new Fold[Id] {
      def apply[T](value: Id[T])(f: (T) => Unit) {
        f(value)
      }
    }
}

trait NestedFold {

  implicit def composeFold[X[+_], Y[+_]](implicit x:Fold[X], y:Fold[Y]):Fold[({type C[+T] = X[Y[T]]})#C] =
    new Fold[({type C[+T] = X[Y[T]]})#C] {
      def apply[T](value: X[Y[T]])(f: (T) => Unit) {
        x(value)(yt => y(yt)(f))
      }
    }
}

trait FutureThrowFailureFold {
  implicit def futureFold(implicit executionContext:ExecutionContext):Fold[Future] =
    new Fold[Future] {
      def apply[T](value: Future[T])(f: (T) => Unit) {
        value.onComplete{
          case util.Failure(ex) =>
            throw ex
          case util.Success(t) =>
            f(t)
        }
      }
    }
}
