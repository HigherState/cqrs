package org.higherState.cqrs

trait Input {
  type In[+T]
}
object Input {
  trait Identity extends Input {
    type In[+T] = T
  }
  trait Future extends Input {
    type In[+T] = scala.concurrent.Future[T]
  }
  trait Valid extends Input {
    type In[+T] = org.higherState.cqrs.Valid[T]
  }
  trait FutureValid extends Input {
    type In[+T] = org.higherState.cqrs.FutureValid[T]
  }
}