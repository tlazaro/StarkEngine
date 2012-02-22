package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.belfrygames.starkengine.utils.Loop

object Sprite {
  def apply(texture: TextureRegion) = {
    new Sprite with Particle {
      textureRegion = texture
    }
  }
  
  val tmp = new Vector3
  val m = new Matrix4
}

trait Sprite extends Drawable with Updateable with Spatial {
  self: Particle =>

  var textureRegion : TextureRegion = null
  
  def width = textureRegion.getRegionWidth.toFloat
  def height = textureRegion.getRegionHeight.toFloat
  
  def setOrigin(xRatio: Float, yRatio: Float) {
    originfX = xRatio
    originfY = yRatio
  }
  
  def isOver(pickX: Float, pickY: Float) : Boolean = {
    Sprite.m.idt()
    Sprite.m.scale(1 / scaleX, 1 / scaleY, 1f)
    Sprite.m.rotate(0, 0, 1f, -rotation)
    Sprite.m.translate(-(x + xOffset), -(y + yOffset), 0f)
    
    Sprite.tmp.x = pickX
    Sprite.tmp.y = pickY
    Sprite.tmp.z = 0
    Sprite.tmp.mul(Sprite.m)
    
    -(originX + xOffset) <= Sprite.tmp.x && Sprite.tmp.x <= -(originX + xOffset) + width &&
    -(originY + yOffset) <= Sprite.tmp.y && Sprite.tmp.y <= -(originY + yOffset) + height
  }
  
  override def draw(spriteBatch : SpriteBatch) {
    if (textureRegion != null)
      spriteBatch.draw(textureRegion, x + xOffset - originX, y + yOffset - originY, originX + xOffset, originY + yOffset, width, height, scaleX, scaleY, rotation)
  }
  
  override def debugDraw(renderer : ShapeRenderer) {
    if (textureRegion != null) {
      val transX = x + xOffset
      val transY = y + yOffset
      
      def bounds() {
        if (selected) {
          renderer.setColor(1f, 0f, 0f, 1f)
        } else {
          renderer.setColor(0f, 1f, 0f, 1f)
        }
        renderer.begin(ShapeType.Rectangle)
        renderer.identity()
        renderer.translate(transX, transY, 0f)
        renderer.rotate(0f, 0f, 1f, rotation)
        renderer.scale(scaleX, scaleY, 1f)
        renderer.rect(-(originX + xOffset), -(originY + yOffset), width, height)
        renderer.end()
      }
      
      def cross() {
        renderer.setColor(1f, 1f, 1f, 1f)
        renderer.begin(ShapeType.Line)
        renderer.identity()
        renderer.translate(transX, transY, 0f)
        renderer.rotate(0f, 0f, 1f, rotation)
        renderer.scale(scaleX, scaleY, 1f)
        renderer.line(-5, -5, 5, 5)
        renderer.line(-5, 5, 5, -5)
        renderer.end()
      }
      
      def pointsInside() {
        renderer.begin(ShapeType.Point)
        for(x <- Range.Double.inclusive(-512, 512, 5); y <- Range.Double.inclusive(-320, 320, 5)) {
          if (isOver(x.toFloat, y.toFloat)) {
            renderer.setColor(1f, 0f, 0f, 0.5f)
            renderer.identity()
            renderer.translate(x.toFloat, y.toFloat, 0f)
            renderer.rotate(0f, 0f, 1f, rotation)
            renderer.scale(scaleX, scaleY, 1f)
            renderer.point(0, 0, 0)
          }
        }
        renderer.end()
      }
      
      bounds()
      cross()
    }
  }
}

class Animation(val frames: Vector[TextureRegion], val duration: Long @@ Milliseconds, val loop: Boolean) extends Loop(duration) {
  
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
  }
  
  def frame = frames((fraction * frames.length).toInt)
}

object AnimatedSprite {
  def apply(anim: Animation) = {
    new AnimatedSprite with Particle {
      animation = anim
    }
  }
  
  val tmp = new Vector3
  val m = new Matrix4
}

trait AnimatedSprite extends Sprite {
  self: Particle =>
  
  var animation : Animation = null
  
  override def update(elapsed: Long @@ Milliseconds) {
    if (animation != null) {
      animation.update(elapsed)
      textureRegion = animation.frame
    }
  }
}
