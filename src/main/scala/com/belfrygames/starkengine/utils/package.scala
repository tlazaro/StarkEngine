package com.belfrygames.starkengine

package object utils {
  def clamp(value: Float, min: Float, max: Float): Float = {
    if (value <= min) {
      min
    } else if (value >= max) {
      max
    } else {
      value
    }
  }
}
