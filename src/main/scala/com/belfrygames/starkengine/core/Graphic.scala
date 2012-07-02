package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
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

/** 
 * Since there is no cohesion among Libgdx primitives this class is needed.
 * This class should be extended to wrap visual primitives like a Texture, TextureRegion and BitmapFont
 */
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

class Tex(override var primitive: Texture) extends Graphic[Texture] {
  private var region = new TextureRegion(primitive)
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null)
      spriteBatch.draw(region, x, y, centerX, centerY, width, height, scaleX, scaleY, rotation)
  }
  
  override def width: Float = if (primitive != null) primitive.getWidth else -1
  override def height: Float = if (primitive != null) primitive.getHeight else -1
}

class Text(
  private var _primitive: BitmapFont,
  var text: String = "",
  var size: Int = 14) extends Graphic[BitmapFont] {
  
  private var bounds: BitmapFont.TextBounds = null
  
  primitive = _primitive
  
  override def primitive_=(value: BitmapFont) = {
    _primitive = value
    if (_primitive != null) {
      bounds = _primitive.getBounds(text)
    } else {
      bounds = null
    }
  }
  override def primitive = _primitive
  
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (_primitive != null) {
      _primitive.setScale(scaleX, scaleY)
      bounds = _primitive.getBounds(text)
      
      _primitive.draw(spriteBatch, text, x, y + bounds.height)
    }
  }
  
  override def width: Float = if (bounds != null) bounds.width else -1
  override def height: Float = if (bounds != null) bounds.height else -1
}
