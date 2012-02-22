package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._
import com.belfrygames.starkengine.utils._
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2

object FollowCamera {
  val EPSILON = 1.0f
}

class FollowCamera(val camera : Camera) extends AcceleratedUpdateable {
  var target : Accelerated = _
  val offset = new Vector2(0.0f, 0.0f)

  var minX = 0.0f
  var minY = 0.0f
  var maxX = Float.MaxValue
  var maxY = Float.MaxValue

  val speed = 0.5f
  var lerp = 0.9f

  val oldDir = new Vector2(0.0f, 0.0f)
  val dir = new Vector2(0.0f, 0.0f)

  override def update(elapsed : Long @@ Milliseconds) {
    super.update(elapsed)
    
    camera.update()

    x = clamp(x, minX, maxX)
    y = clamp(y, minY, maxY)

    if (target != null) {
      camera.position.x = x
      camera.position.y = y

      oldDir.x = dir.x
      oldDir.y = dir.y

      dir.x = offset.x + target.x - x
      dir.y = offset.y + target.y - y

      val distance = dir.len
      if (distance >= FollowCamera.EPSILON) {
        dir.nor.mul(math.min(speed, distance)).lerp(oldDir, lerp)

        xSpeed = dir.x
        ySpeed = dir.y
      } else {
        x = offset.x + target.x
        y = offset.y + target.y
        
        xSpeed = 0
        ySpeed = 0
      }
    }
  }

  def clamp (a : Float, min : Float, max : Float) = {
    val res = if (a <= min) min else a
    if (res <= max) res else max
  }
}

class ScreenCam(val cam: OrthographicCamera) extends DrawableParent {
  override def draw (spriteBatch: SpriteBatch) {
    val m = spriteBatch.getProjectionMatrix.cpy
    spriteBatch.setProjectionMatrix(cam.combined)
    spriteBatch.begin()
    drawChildren(spriteBatch)
    spriteBatch.end()
    spriteBatch.setProjectionMatrix(m)
  }
}

class FakeCam extends ScreenCam(null) {
  override def draw (spriteBatch: SpriteBatch) {
    spriteBatch.begin()
    drawChildren(spriteBatch)
    spriteBatch.end()
  }
}
