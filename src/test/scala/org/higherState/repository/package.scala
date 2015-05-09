package org.higherState

import org.higherState.cqrs.QueryExecutor

/**
 * Created by Jamie Pullar on 09/05/2015.
 */
package object repository {
  trait TF {
    type I[Y]
  }
  type Kvqe[Key, Value] = TF { type I[Y] = KeyValueQueryParameters[Y, Key, Value] }
}
