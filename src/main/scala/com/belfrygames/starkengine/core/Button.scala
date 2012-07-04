package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._

class Button(val up: Node, val over: Node, val down: Node, val hit: Node) extends Node {
  object ButtonState extends Enumeration {
    type ButtonState = Value
    val UP, OVER, DOWN = Value
  }
  import ButtonState._
  
  var state: ButtonState.Value = UP
  var current: Node = null
  
  /** Init */
  updateState()
  
  override def width = current.width
  override def height = current.height
  
  // Selects the current node to display based on the state
  def updateState() {
    state = if (selected) OVER else UP
    
    val prev = current
    current = state match {
      case UP => up
      case OVER => over
      case DOWN => down
    }
    
    if (prev != current) {
      remove(prev)
      add(current, "state")
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