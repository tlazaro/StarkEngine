package com.starkengine.utils

import com.badlogic.gdx.utils.Pool

/**
 * A skill for a Pool making it thread safe.
 * @author Tomás Lázaro
 */
trait SynchronizedPool[T] extends Pool[T] {
  /**
   * Returns an object from this pool. The object may be new (from {@link #newObject()}) or reused (previously
   * {@link #free(Object) freed}).
   */
  abstract override def obtain(): T = synchronized {
    super.obtain()
  }

  /**
   * Puts the specified object in the pool, making it eligible to be returned by {@link #obtain()}. If the pool already contains
   * {@link #max} free objects, the specified object is ignored.
   */
  abstract override def free(obj: T) = synchronized {
    super.free(obj)
  }

  /**
   * Puts the specified objects in the pool.
   * @see #free(Object)
   */
  abstract override def free(objects: com.badlogic.gdx.utils.Array[T]) = synchronized {
    super.free(objects)
  }

  /** Removes all free objects from this pool. */
  abstract override def clear() = synchronized {
    super.clear()
  }
}
