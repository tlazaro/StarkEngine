package com.belfrygames.starkengine

package object tags {
  type Tagged[U] = { type Tag = U }
  type @@[T, U] = T with Tagged[U]
  
  class Nanoseconds
  class Milliseconds
  class Seconds
  
  class Tagger[U] {
    @inline def apply[T](t : T) : T @@ U = t.asInstanceOf[T @@ U]
  }
  
  @inline def tag[U] = new Tagger[U]

  // Manual specialization needed here ... specializing apply above doesn't help
  @inline def tag[U](c : Char) : Char @@ U = c.asInstanceOf[Char @@ U]
  @inline def tag[U](s : Short) : Short @@ U = s.asInstanceOf[Short @@ U]
  @inline def tag[U](i : Int) : Int @@ U = i.asInstanceOf[Int @@ U]
  @inline def tag[U](l : Long) : Long @@ U = l.asInstanceOf[Long @@ U]
  @inline def tag[U](f : Float) : Float @@ U = f.asInstanceOf[Float @@ U]
  @inline def tag[U](d : Double) : Double @@ U = d.asInstanceOf[Double @@ U]
}
