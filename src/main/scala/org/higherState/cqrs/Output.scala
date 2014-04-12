package org.higherState.cqrs

trait Output {
  type Out[+T]
}

trait Input {
  type In[+T]
}