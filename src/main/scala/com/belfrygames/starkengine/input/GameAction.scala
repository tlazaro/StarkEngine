package com.belfrygames.starkengine.input

import scala.collection.mutable.ArrayBuffer

object GameAction {
  /**
   * Normal behavior. The isPressed() method returns true
   * as long as the key is held down.
   */
  val NORMAL = 0
  
  /**
   * Initial press behavior. The isPressed() method returns
   * true only after the key is first pressed, and not again
   * until the key is released and pressed again.
   */
  val DETECT_INITAL_PRESS_ONLY = 1

  val STATE_RELEASED = 0
  val STATE_PRESSED = 1
  val STATE_WAITING_FOR_RELEASE = 2
}

class GameAction(val name: String, val behavior: Int = GameAction.NORMAL) {
  import GameAction._
  
  private[this] var amount: Int = _
  private[this] var state: Int = _
  private[this] val actions = new ArrayBuffer[() => Unit]
  
  reset()
  
  def appendAction(f: () => Unit) {
    actions += f
  }
    
  /**
   * Resets this GameAction so that it appears like it hasn't
   * been pressed.
   */
  def reset(): Unit = synchronized {
    state = STATE_RELEASED
    amount = 0
  }
  
  /**
   * Taps this GameAction. Same as calling press() followed
   * by release().
   */
  def tap(): Unit = synchronized {
    press()
    release()
  }
  
  /**
   * Signals that the key was pressed.
   */
  def press(): Unit = synchronized {
    press(1)
  }
  
  /**
   * Signals that the key was pressed a specified number of
   * times, or that the mouse moved a specified distance.
   */
  def press(amount: Int): Unit = synchronized {
    if (state != STATE_WAITING_FOR_RELEASE) {
      this.amount += amount
      state = STATE_PRESSED
      actions.foreach(_.apply)
    }
  }
  
  /**
   * Signals that the key was released
   */
  def release(): Unit = synchronized {
    state = STATE_RELEASED
  }

  /**
   * Returns whether the key was pressed or not since last
   * checked.
   */
  def isPressed(): Boolean = synchronized {
    getAmount() != 0
  }

  /**
   * For keys, this is the number of times the key was
   * pressed since it was last checked.
   * For mouse movement, this is the distance moved.
   */
  def getAmount(): Int = synchronized {
    val retVal = amount
    if (retVal != 0) {
      if (state == STATE_RELEASED) {
        amount = 0
      } else if (behavior == DETECT_INITAL_PRESS_ONLY) {
        state = STATE_WAITING_FOR_RELEASE
        amount = 0
      }
    }
    return retVal
  }
  
  def evaluatePosition(x: Int, y: Int, pointer: Int) : Boolean = true
}


