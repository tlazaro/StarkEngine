package com.belfrygames.starkengine.map

trait MapListener {
  def mapChanged(map: StarkMap): Unit
}
