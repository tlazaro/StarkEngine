package com.belfrygames.starkengine.utils

import com.belfrygames.starkengine.tags._
import com.belfrygames.starkengine.core._

/**
 * Simple StopWatch to handle time
 */
class StopWatch {
  private[this] var start = 0L
  private[this] var end = 0L
  
  def start() { this.start = System.nanoTime }
  def end() { this.end = System.nanoTime }
  
  def diff = tag[Nanoseconds](this.end - this.start)
  def diffMillis = tag[Milliseconds](diff / 1000000L)
  def diffSeconds = tag[Seconds](diff / 1000000000L)
}

class Cooldown(val lapse : Long @@ Milliseconds) extends Updateable {
  private[this] var acc = 0L
  
  override def update(elapsed : Long @@ Milliseconds) {
    acc += elapsed
  }
  
  def reset(): Unit = acc = 0
  def ready() : Boolean = acc >= lapse
  def fraction(): Float = clamp(acc.toFloat / lapse.toFloat, 0.0f, 1.0f)
}

class Loop(val lapse : Long @@ Milliseconds) extends Updateable {
  protected var acc = 0L
  
  override def update(elapsed : Long @@ Milliseconds) {
    acc += elapsed
    if (acc >= lapse) {
      acc -= lapse
    }
  }
  
  def reset(): Unit = acc = 0
  def fraction(): Float = clamp(acc.toFloat / lapse.toFloat, 0.0f, 1.0f)
}

class CounterWatch(val period : Long @@ Nanoseconds) extends StopWatch {
  private[this] var ticks = 0
  private[this] var total = 0
  def tick() = {
    end()
    
    if (diff >= period) {
      start()
      total = ticks
      ticks = 0
    } else {
      ticks += 1
    }
  }
  
  def totalTicks = total
}

object StopWatch {
  def measure(block : => Unit) = {
    val start = System.nanoTime
    block
    tag[Milliseconds]((System.nanoTime - start) / 1000000L)
  }
}