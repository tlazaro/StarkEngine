package com.belfrygames.starkengine.input

import com.badlogic.gdx.InputProcessor

object InputManager extends InputProcessor {
  val NUM_MOUSE_CODES = 9
  val NUM_KEY_CODES = 600
  
  private val keyActions = new Array[GameAction](NUM_KEY_CODES)
  private val mouseActions = new Array[GameAction](NUM_MOUSE_CODES)
  
  /**
   * Maps a GameAction to a specific key. The key codes are defined in
   * java.awt.KeyEvent. If the key already has a GameAction mapped to it, the
   * new GameAction overwrites it.
   */
  def mapToKey(gameAction: GameAction, keyCode: Int) {
    keyActions(keyCode) = gameAction
  }

  /**
   * Maps a GameAction to a specific mouse action. The mouse codes are defined
   * here in InputManager (MOUSE_MOVE_LEFT, MOUSE_BUTTON_1, etc). If the mouse
   * action already has a GameAction mapped to it, the new GameAction
   * overwrites it.
   */
  def mapToMouse(gameAction: GameAction, mouseCode: Int) {
    mouseActions(mouseCode) = gameAction
  }
  
  /**
   * Clears all mapped keys and mouse actions to this GameAction.
   */
  def clearMap(gameAction: GameAction) {
    for (i <- 0 until keyActions.length) {
      if (keyActions(i) == gameAction) {
        keyActions(i) = null
      }
    }

    for (i <- 0 until mouseActions.length) {
      if (mouseActions(i) == gameAction){
        mouseActions(i) = null
      }
    }

    gameAction.reset()
  }
  
  /**
   * Gets a List of names of the keys and mouse actions mapped to this
   * GameAction. Each entry in the List is a String.
   */
  def getMaps(gameCode: GameAction) = {
    (keyActions.view.filter(_ == gameCode) ++ (mouseActions.view.filter(_ == gameCode))).toList
  }
  
  /**
   * Resets all GameActions so they appear like they haven't been pressed.
   */
  def resetAllGameActions() {
    val reset = { g: GameAction => if (g != null) g.reset() }
    _for(keyActions)(reset)
    _for(mouseActions)(reset)
  }
  
  @inline def _for[T](array: Array[T])(f: T => _) = {
    var i = 0
    while(i < array.length) {
      f(array(i))
      i += 1
    }
  }
  
  private def getKeyAction(keyCode : Int): Option[GameAction] = {
    if (keyCode != -1) {
      val gameAction = keyActions(keyCode)
      if (gameAction != null) {
        return Some(gameAction)
      }
    }
    None
  }
  
  private def getMouseButtonAction(mouseCode : Int): Option[GameAction]  = {
    if (mouseCode != -1) {
      val gameAction = mouseActions(mouseCode)
      if (gameAction != null) {
        return Some(gameAction)
      }
    }
    None
  }
  
  override def keyDown (keycode : Int) : Boolean = {
    getKeyAction(keycode) foreach { _.press() }
    true
  }

  override def keyUp (keycode : Int) : Boolean = {
    getKeyAction(keycode) foreach { _.release() }
    true
  }

  override def keyTyped (character : Char) = false
  
  override def touchDown (x: Int, y: Int, pointer: Int, button: Int): Boolean = {
    getMouseButtonAction(button) match {
      case Some(action) => if (action.evaluatePosition(x, y, pointer)) {
          action.press()
          true
        } else {
          false
        }
      case _ => false
    }
  }
  
  override def touchUp (x : Int, y : Int, pointer : Int, button : Int): Boolean = {
    getMouseButtonAction(button) match {
      case Some(action) => if (action.evaluatePosition(x, y, pointer)) {
          action.release()
          true
        } else {
          false
        }
      case _ => false
    }
  }

  override def touchDragged (x: Int, y: Int, pointer: Int): Boolean = {
    touchMoved(x, y)
  }
  override def touchMoved (x: Int, y: Int): Boolean = false
  override def scrolled (amount: Int): Boolean = false
}
