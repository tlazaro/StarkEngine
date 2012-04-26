package com.belfrygames.starkengine

package object utils {
  @inline def clamp(value: Int, min: Int, max: Int): Int = {
    if (value <= min) min else if (value >= max) max else value
  }
  @inline def clamp(value: Float, min: Float, max: Float): Float = {
    if (value <= min) min else if (value >= max) max else value
  }
  
  @inline def between(value: Float, min: Float, max: Float): Boolean = min <= value && value <= max
  @inline def between(value: Int, min: Int, max: Int): Boolean = min <= value && value <= max
  
  @inline def betweenStrict(value: Float, min: Float, max: Float): Boolean = min < value && value < max
  @inline def betweenStrict(value: Int, min: Int, max: Int): Boolean = min < value && value < max
}
