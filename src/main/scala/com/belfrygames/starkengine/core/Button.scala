package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._

object ButtonState extends Enumeration {
	type ButtonState = Value
	val UP, OVER, DOWN = Value
}

class Button(val up: Node, val over: Node, val down: Node, val hit: Node, val text: Node = null) extends Node {
  import ButtonState._
  
  var state: ButtonState.Value = UP
  var current: Node = null
  
  /** Init */
  updateState()
  
  override def width = current.width
  override def height = current.height
  
  /** Button always returns None to force the whole button to be the one receiving mouse events. */
  override def isOverChildren(pickX: Float, pickY: Float): Option[Node] = None
  
  // Selects the current node to display based on the state
  def updateState() {
	if (!selected) {
		state = UP
	}
	
    val prev = current
    current = state match {
      case UP => up
      case OVER => over
      case DOWN => down
    }
    
    if (prev != current) {
      remove(prev)
      add(current, "state")
      
      if (text != null) {
        remove(text)
        add(text, "text")
      }
    }
  }
  
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    updateState()
  }
}

object Button {
  def apply(up: Graphic[_], over: Graphic[_], down: Graphic[_], hit: Graphic[_]): Button = {
    new Button(Sprite(up), Sprite(over), Sprite(down), Sprite(hit))
  }
}