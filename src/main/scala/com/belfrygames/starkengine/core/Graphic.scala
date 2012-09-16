package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.stbtt.TrueTypeFontFactory
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.Gdx

object Graphic {
  def apply(region: TextureRegion) = {
    region match {
      case t: TextureAtlas.AtlasRegion => new TextureAtlasRegion(t)
      case t => new Region(t)
    }
  }

  def apply(texture: Texture) = {
    new Tex(texture)
  }

  lazy val SQUARE: Graphic[_] = {
    import com.badlogic.gdx.graphics.{ Texture, Pixmap }
    import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap
    val pix = new Gdx2DPixmap(1, 1, Gdx2DPixmap.GDX2D_FORMAT_RGBA8888)
    pix.clear(0xFFFFFFFF)
    val tex = new Texture(pix.getWidth(), pix.getHeight(), Pixmap.Format.RGBA8888)
    tex.draw(new Pixmap(pix), 0, 0)
    new Tex(tex)
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
    if (primitive != null) {
      spriteBatch.draw(primitive, x, y, centerX, centerY, width, height, scaleX, scaleY, rotation)
    }
  }

  override def width: Float = if (primitive != null) primitive.getRegionWidth else -1
  override def height: Float = if (primitive != null) primitive.getRegionHeight else -1
}

class TextureAtlasRegion(override var primitive: TextureAtlas.AtlasRegion) extends Graphic[TextureAtlas.AtlasRegion] {
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null) {
      spriteBatch.draw(primitive, x + primitive.offsetX, y + primitive.offsetY, centerX, centerY,
        primitive.packedWidth, primitive.packedHeight, scaleX, scaleY, rotation)
    }
  }

  override def width: Float = if (primitive != null) primitive.originalWidth else -1
  override def height: Float = if (primitive != null) primitive.originalHeight else -1
}

class Tex(override var primitive: Texture) extends Graphic[Texture] {
  private var region = new TextureRegion(primitive)
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null) {
      spriteBatch.draw(region, x, y, centerX, centerY, width, height, scaleX, scaleY, rotation)
    }
  }

  override def width: Float = if (primitive != null) primitive.getWidth else -1
  override def height: Float = if (primitive != null) primitive.getHeight else -1
}

object Text {
  val FONT_CHARACTERS =
    """abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_¡!$%#@|\/¿?-+=()*&.;:,{}"´`'<>áéíóúüÁÉÍÓÚÜñÑ"""

  def getText(pixelSize: Int, text: String): TrueTypeText = {
    new TrueTypeText(getFont(pixelSize), text)
  }

  def getFont(pixelSize: Int): BitmapFont = {
    val font = TrueTypeFontFactory.createBitmapFont(
      Gdx.files.internal("com/belfrygames/starkengine/ubuntu.ttf"),
      FONT_CHARACTERS, 1024, 640, pixelSize, 1024, 640)

    font.setUseIntegerPositions(false)
    font
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
      val textx = x + centerX - scaleX * centerX
      val texty = y + centerY - scaleY * centerY + height * scaleY
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
      val textx = x + centerX - scaleX * centerX
      val texty = y + centerY - scaleY * centerY + height * scaleY - _primitive.getLineHeight
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
