package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._
import java.io.FileNotFoundException

object ButtonState extends Enumeration {
  type ButtonState = Value
  val UP, OVER, DOWN, DISABLED = Value
}

class Button(val up: Node, val over: Node, val down: Node, val disabled: Node, val hit: Node, val text: Node = null) extends Node {
  import ButtonState._

  var state: ButtonState.Value = UP
  var current: Node = null
  val states = up :: over :: down :: disabled :: hit :: (if (text != null) text :: Nil else Nil)

  /** Init */
  updateState()

  override def width = current.width
  override def height = current.height

  /** Button lies if mouse is over a children to be the one receiving mouse events. */
  override def isOverChildren(pickX: Float, pickY: Float, strat: OverStrategy, behavior: OverBehavior): Option[Node] = {
    super.isOverChildren(pickX, pickY, strat, behavior).map(child => this)
  }
  
  // Selects the current node to display based on the state
  def updateState() {
    if (!enabled) {
      state = DISABLED
    } else if (!selected) {
      state = UP
    }

    val prev = current
    current = state match {
      case UP => up
      case OVER => over
      case DOWN => down
      case DISABLED => disabled
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
  def apply(up: Graphic[_], over: Graphic[_], down: Graphic[_], disabled: Graphic[_], hit: Graphic[_], text: Node = null): Button = {
    new Button(Sprite(up), Sprite(over), Sprite(down), Sprite(disabled), Sprite(hit), text)
  }

  val suffixes = Map(
    "up" -> List("", "up", "normal"),
    "over" -> List("over", "mouseover", "highlight", "high"),
    "down" -> List("down", "pressed", "pushed"),
    "disabled" -> List("disabled", "grayed", "gray"),
    "hit" -> List("hit", "hitarea", "mask"))

  def create(id: String, text: => Node): Either[Throwable, Button] = {
    def findSuffixes(state: String) = suffixes(state).flatMap(s => List(id + "_" + s, id + s)).find(Resources.hasImage(_))
    def getGraphic(img: String) = Graphic(Resources.load(img))

    val up = findSuffixes("up")
    up match {
      case Some(path) =>
        val img = getGraphic(path)
        val over = findSuffixes("over").map(getGraphic(_))
        val down = findSuffixes("down").map(getGraphic(_))
        val disabled = findSuffixes("disabled").map(getGraphic(_))
        val hit = findSuffixes("hit").map(getGraphic(_))
        Right(Button(img, over.getOrElse(img), down.getOrElse(img), disabled.getOrElse(img), hit.getOrElse(img), text))
      case _ => Left(new FileNotFoundException("Image " + id + " for button not found."))
    }
  }
}