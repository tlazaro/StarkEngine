package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.stbtt.TrueTypeFontFactory
import com.badlogic.gdx.Gdx

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
  var color: Color = Color.WHITE.cpy // TODO: Move color to Node. Set color on spriteBatch. Have Text get Color from spritebatch
  var primitive: T
  def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float = 0, centerY: Float = 0, width: Float = width, height: Float = height, scaleX: Float = 1.0f, scaleY: Float = 1.0f, rotation: Float = 0)
  
  def width: Float
  def height: Float
}

class Region(override var primitive: TextureRegion) extends Graphic[TextureRegion] {
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null) {
      spriteBatch.setColor(color)
      spriteBatch.draw(primitive, x, y, centerX, centerY, width, height, scaleX, scaleY, rotation)
    }
  }
  
  override def width: Float = if (primitive != null) primitive.getRegionWidth else -1
  override def height: Float = if (primitive != null) primitive.getRegionHeight else -1
}

class Tex(override var primitive: Texture) extends Graphic[Texture] {
  private var region = new TextureRegion(primitive)
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null) {
      spriteBatch.setColor(color)
      spriteBatch.draw(region, x, y, centerX, centerY, width, height, scaleX, scaleY, rotation)
    }
  }
  
  override def width: Float = if (primitive != null) primitive.getWidth else -1
  override def height: Float = if (primitive != null) primitive.getHeight else -1
}

object Text {
  val FONT_CHARACTERS = """abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\/?-+=()*&.;,{}"´`'<>"""
  
  def getText(pixelSize: Int, text: String): TrueTypeText = {
    new TrueTypeText(getFont(pixelSize), text)
  }
  
  def getFont(pixelSize: Int): BitmapFont = {
    TrueTypeFontFactory.createBitmapFont(
      Gdx.files.internal("com/belfrygames/starkengine/ubuntu.ttf"),
      FONT_CHARACTERS, 1024, 640, pixelSize, 1024, 640)
  }
}

class BitmapText(private var _primitive: BitmapFont, var text: String = "") extends Graphic[BitmapFont] {
  private var bounds: BitmapFont.TextBounds = null
  
  primitive = _primitive
  
  override def primitive_=(value: BitmapFont) = {
    _primitive = value
    if (_primitive != null) {
      bounds = _primitive.getBounds(text)
      bounds = _primitive.getMultiLineBounds(text)
    } else {
      bounds = null
    }
  }
  override def primitive = _primitive
  
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (_primitive != null) {
      _primitive.setScale(scaleX, scaleY)
      val textx = scala.math.round(x + centerX - scaleX * centerX)
      val texty = scala.math.round(y + centerY - scaleY * centerY + height * scaleY)
      _primitive.setColor(color)
      _primitive.drawMultiLine(spriteBatch, text, textx, texty)
    }
  }
  
  override def width: Float = {
    if (bounds != null) {
      (bounds.width / _primitive.getScaleX)
    } else -1
  }
  
  override def height: Float = {
    if (_primitive != null) {
      ((_primitive.getLineHeight * text.lines.size) / _primitive.getScaleY)
    } else -1
  }
}

class TrueTypeText(private var _primitive: BitmapFont, var text: String = "") extends Graphic[BitmapFont] {
  private var bounds: BitmapFont.TextBounds = null
  
  primitive = _primitive
  
  override def primitive_=(value: BitmapFont) = {
    _primitive = value
    if (_primitive != null) {
      bounds = _primitive.getBounds(text)
      bounds = _primitive.getMultiLineBounds(text)
    } else {
      bounds = null
    }
  }
  override def primitive = _primitive
  
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (_primitive != null) {
      _primitive.setScale(scaleX, scaleY)
      val textx = scala.math.round(x + centerX - scaleX * centerX)
      val texty = scala.math.round(y + centerY - scaleY * centerY + height * scaleY - _primitive.getLineHeight)
      _primitive.setColor(color)
      _primitive.drawMultiLine(spriteBatch, text, textx, texty)
    }
  }
  
  override def width: Float = {
    if (bounds != null) {
      (bounds.width / _primitive.getScaleX)
    } else -1
  }
  
  override def height: Float = {
    if (_primitive != null) {
      ((_primitive.getLineHeight * text.lines.size) / _primitive.getScaleY)
    } else -1
  }
}
