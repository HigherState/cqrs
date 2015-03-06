package org.higherState

import scalaz._

package object cqrs {
  trait TF {
    type I[Y[+_]]
  }
  type ~>![X[+_]] = TF { type I[Y[+_]] = ~>[Y, X] }
}
