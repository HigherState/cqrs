package org.higherState.cqrs

import scalaz._

trait Output {
  type Out[+T]
}
object Output {
  trait Identity extends Output {
    type Out[+T] = T
  }
  trait Future extends Output {
    type Out[+T] = scala.concurrent.Future[T]
  }
  trait Valid extends Output {
    type Out[+T] = org.higherState.cqrs.Valid[T]
  }
  trait FutureValid extends Output {
    type Out[+T] = org.higherState.cqrs.FutureValid[T]
  }
}