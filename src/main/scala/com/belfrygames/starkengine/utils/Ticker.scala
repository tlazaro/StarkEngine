package com.belfrygames.plat.utils

trait Ticker {
  var ticks = 0L
  
  def tick() { ticks += 1 }
}
