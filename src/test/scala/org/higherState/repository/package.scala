package org.higherState

package object repository {
  trait TF {
    type I[Y]
  }
  type Kvqe[Key, Value] = TF { type I[Y] = KeyValueQueryParameters[Y, Key, Value] }
}
