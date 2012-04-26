package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.belfrygames.starkengine.utils.Loop

object Sprite {
  def apply(graphic: Graphic[_]) = {
    new Sprite(graphic)
  }
  def apply(texture: TextureRegion) = {
    new Sprite(Graphic(texture))
  }
  def apply(texture: Texture) = {
    new Sprite(Graphic(texture))
  }
}

class Sprite(_graphic: Graphic[_]) extends Node {
  graphic = _graphic
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

class AnimatedSprite(var animation : Animation = null) extends Sprite(null) {
  override def update(elapsed: Long @@ Milliseconds) {
    if (animation != null) {
      animation.update(elapsed)
      graphic = Graphic(animation.frame)
    }
  }
}
