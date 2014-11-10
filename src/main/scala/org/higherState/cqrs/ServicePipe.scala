package org.higherState.cqrs

object ServicePipe {

  implicit def impl[In[+_], Out[+_],T](value:In[T])(implicit pipe:Pipe[In,Out]):Out[T]
    = pipe.apply(value)
}
