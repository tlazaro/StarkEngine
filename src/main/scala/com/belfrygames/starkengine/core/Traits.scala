package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.belfrygames.starkengine.utils._
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.belfrygames.starkengine.tags._

trait Updateable {
  def update(elapsed : Long @@ Milliseconds) {

  }
}

object Timed {
  val NS_IN_S = 1000000000L
  val NS_IN_MS = 1000000L
}

trait Timed { self: Updateable =>
  import Timed._

  private var nanos = 0L

  // Time in milliseconds
  var time = 0L

  final def reset() {
    nanos = 0L
    time = 0L
  }

  final def nanoUpdate(elapsedNanos : Long @@ Nanoseconds) {
    nanos += elapsedNanos

    val increment = nanos / NS_IN_MS

    time += increment
    nanos = nanos % NS_IN_MS

    update(tag[Milliseconds](increment))
  }
}

trait Particle {
  var x = 0.0f
  var y = 0.0f
  var z = 0.0f
}

trait Dynamic extends Particle {
  var xSpeed = 0.0f
  var ySpeed = 0.0f
}

trait Accelerated extends Dynamic {
  var xAccel = 0.0f
  var yAccel = 0.0f
}

trait DynamicUpdatable extends Updateable with Dynamic {
  abstract override def update(elapsed : Long @@ Milliseconds) {
    super.update(elapsed)

    x += elapsed * xSpeed
    y += elapsed * ySpeed
  }
}

trait AcceleratedUpdateable extends Updateable with Accelerated {
  abstract override def update(elapsed : Long @@ Milliseconds) {
    super.update(elapsed)

    xSpeed += elapsed * xAccel
    ySpeed += elapsed * yAccel

    x += elapsed * xSpeed
    y += elapsed * ySpeed
  }
}

trait Spatial {
  def width : Float
  def height : Float
  
  def originX = width * originfX
  def originY = height * originfY
  
  var originfX = 0f
  var originfY = 0f
  
  var scaleX = 1f
  var scaleY = 1f
  
  var rotation: Float = 0f
}

trait Drawable {  
  var xOffset = 0
  var yOffset = 0
  var visible = true
  var selected = false
  
  final def redraw (spriteBatch : SpriteBatch) {
    if (visible) {
      draw(spriteBatch)
    }
  }
  
  final def debugRedraw(renderer: ShapeRenderer) {
    if (visible) {
      debugDraw(renderer)
    }
  }
  
  protected def draw(spriteBatch: SpriteBatch)
  protected def debugDraw(renderer: ShapeRenderer)
}

trait UpdateableParent extends Updateable {
  @volatile var updateables = Vector[Updateable]()
  
  final def addUpdateable[T<:Updateable](child : T): T = synchronized {
    updateables = updateables :+ child
    child
  }
  
  final def removeUpdateable[T<:Updateable](child : T) = synchronized {
    updateables indexOf child match {
      case n if n >= 0 => {
        val (prefix, suffix) = updateables splitAt n
        updateables = prefix ++ suffix.tail
      }
      case _ => sys.error("Child does not exist:" + child)
    }
  }
  
  final def updateChildren(elapsed : Long @@ Milliseconds) = {
    updateables foreach (_ update elapsed)
  }
  
  override def update(elapsed : Long @@ Milliseconds) {
    updateChildren(elapsed)
  }
}

trait DrawableParent extends Drawable {
  @volatile var drawables = Vector[Drawable]()
  
  final def addDrawable[T<:Drawable](child : T): T = synchronized {
    drawables = drawables :+ child
    child
  }
  
  final def removeDrawable[T<:Drawable](child : T) = synchronized {
    drawables indexOf child match {
      case n if n >= 0 => {
        val (prefix, suffix) = drawables splitAt n
        drawables = prefix ++ suffix.tail
      }
      case _ => sys.error("Child does not exist:" + child)
    }
  }
  
  final def drawChildren(spriteBatch: SpriteBatch) = {
    drawables foreach (_ redraw spriteBatch)
  }
  
  final def debugDrawChildren(renderer: ShapeRenderer) = {
    drawables foreach (_ debugRedraw renderer)
  }
  
  override def draw(spriteBatch: SpriteBatch) {
    drawChildren(spriteBatch)
  }
  
  override def debugDraw(renderer: ShapeRenderer) {
    debugDrawChildren(renderer)
  }
}
