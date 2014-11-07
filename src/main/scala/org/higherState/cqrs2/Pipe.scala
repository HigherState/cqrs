package org.higherState.cqrs2

trait Pipe[In[+_], Out[+_]] {

  def apply[In[_], T](f: => In[T]):Out[T]
}
