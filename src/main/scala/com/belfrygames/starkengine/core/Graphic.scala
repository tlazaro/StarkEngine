package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

object Graphic {
  def apply(region: TextureRegion) = {
    new Region(region)
  }
  def apply(texture: Texture) = {
    new Tex(texture)
  }
}

/** It wraps a visual primitive like a Texture a or TextureRegion */
trait Graphic[T <: AnyRef] {
  var primitive: T
  def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float = 0, centerY: Float = 0, width: Float = width, height: Float = height, scaleX: Float = 1.0f, scaleY: Float = 1.0f, rotation: Float = 0)
  
  def width: Float
  def height: Float
}

class Region(override var primitive: TextureRegion) extends Graphic[TextureRegion] {
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null)
      spriteBatch.draw(primitive, x, y, centerX, centerY, width, height, scaleX, scaleY, rotation)
  }
  
  override def width: Float = if (primitive != null) primitive.getRegionWidth else -1
  override def height: Float = if (primitive != null) primitive.getRegionHeight else -1
}

class Tex(override var primitive: Texture) extends Graphic[Texture]  {
  private var region = new TextureRegion(primitive)
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null)
      spriteBatch.draw(region, x, y, centerX, centerY, width, height, scaleX, scaleY, rotation)
  }
  
  override def width: Float = if (primitive != null) primitive.getWidth else -1
  override def height: Float = if (primitive != null) primitive.getHeight else -1
}
