package org.higherState.cqrs

import scalaz.~>

object ServicePipe {

  implicit def ~>[In[+_], Out[+_],T](value:In[T])(implicit nt: ~>[In,Out]):Out[T]
    = nt.apply(value)
}
