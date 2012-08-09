package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._
import com.belfrygames.starkengine.utils._
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2

class FollowCamera(val camera : Camera) extends Updateable {
  var target : Particle = _
  val offset = new Vector2(0.0f, 0.0f)
  var lerpCoef = 0.1f
  var minX = 0.0f
  var minY = 0.0f
  var maxX = Float.MaxValue
  var maxY = Float.MaxValue
  var x = 0.0f
  var y = 0.0f

  override def update(elapsed : Long @@ Milliseconds) {
    //super.update(elapsed)
    camera.update()
    
    if (target != null) {
      x = (offset.x + target.x) * lerpCoef + x * (1 - lerpCoef)
      y = (offset.y + target.y) * lerpCoef + y * (1 - lerpCoef)
      x = clamp(x, minX, maxX)
      y = clamp(y, minY, maxY)  
      camera.position.x = x
      camera.position.y = y
    }
  }
  
  def clamp (a : Float, min : Float, max : Float) = {
    if (a <= min) min else if(a >= max) max else a
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
