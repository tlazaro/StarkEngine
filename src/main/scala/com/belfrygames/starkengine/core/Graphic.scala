package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.stbtt.TrueTypeFontFactory
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.Gdx
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GLContext
import org.lwjgl.opengl.ARBPixelBufferObject
import org.lwjgl.opengl.ARBBufferObject
import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer

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

  private[core] val tempColor = new Color()

  private[this] val m_iWidth = 1
  private[this] val m_iHeight = 1
  private[this] val PBO_WIDTH = 1024
  private[this] val PBO_HEIGHT = 1024
  private[this] val m_bbPixels: ByteBuffer = ByteBuffer.allocateDirect(PBO_WIDTH * PBO_HEIGHT * 16);
  private[this] val m_pixels = new Array[Int](PBO_WIDTH * PBO_HEIGHT)
  private[this] var createdPBO = false
  private[this] var m_iPBOID = 0

  def CreatePBO() {
    if (createdPBO)
      return

    createdPBO = true
    if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
      val CHANNEL_COUNT: Int = 4;
      val DATA_SIZE: Long = PBO_WIDTH * PBO_HEIGHT * CHANNEL_COUNT; //width & height of texture

      val buffer = BufferUtils.createIntBuffer(1);
      org.lwjgl.opengl.ARBBufferObject.glGenBuffersARB(buffer)
      m_iPBOID = buffer.get(0)

      ARBBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, m_iPBOID)
      ARBBufferObject.glBufferDataARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, DATA_SIZE, ARBBufferObject.GL_STREAM_READ_ARB)
      ARBBufferObject.glBindBufferARB(ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB, 0)
    } else {
      println("Error creating PBO")
    }
  }

  def PBOReadPixels(x: Int, y: Int, width: Int, height: Int, glHandle: Int): Int = {
    //    CreatePBO()
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, glHandle);
    GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, m_bbPixels)
    val buff = m_bbPixels.asIntBuffer()
    m_bbPixels.asIntBuffer().get(m_pixels)
    buff.get(m_pixels, 0, m_pixels.length)
    m_pixels(x + (y * width))

    //    import ARBPixelBufferObject._
    //    GL11.glBindTexture(GL11.GL_TEXTURE_2D, glHandle)
    //    GL11.glReadBuffer(GL11.GL_FRONT); //but i want to read from an image, not the front buffer?

    //    // Bind
    //    ARBBufferObject.glBindBufferARB(GL_PIXEL_PACK_BUFFER_ARB, m_iPBOID) //bind
    //
    //    GL11.glBindTexture(GL11.GL_TEXTURE_2D, glHandle)
    //    //GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, m_bbPixels);
    //    GL11.glReadPixels(x, y, m_iWidth, m_iHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, m_bbPixels)
    //    m_bbPixels.asIntBuffer().get(m_pixels);
    //
    //    // Unbind
    //    ARBBufferObject.glBindBufferARB(GL_PIXEL_PACK_BUFFER_ARB, 0)
    //
    //    println("WORKED ONCE!")

    //    ARBBufferObject.glBindBufferARB(GL_PIXEL_PACK_BUFFER_ARB, m_iPBOID)
    //    ARBBufferObject.glBufferDataARB(GL_PIXEL_PACK_BUFFER_ARB, PBO_WIDTH * PBO_HEIGHT * 4, ARBBufferObject.GL_STREAM_READ_ARB)
    ////    GL11.glEnable(GL11.GL_TEXTURE_2D)
    ////    GL11.glActiveTexture(GL_TEXTURE0_ARB)
    //    GL11.glBindTexture(GL11.GL_TEXTURE_2D, glHandle)
    //    GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, m_bbPixels)
    //    
    //    ARBBufferObject.glBindBufferARB(GL_PIXEL_PACK_BUFFER_ARB, 0)
    ////    GL11.glDisable(GL11.GL_TEXTURE_2D)

    m_pixels(0)
  }
}

sealed trait OverStrategy
case object Bounds extends OverStrategy
case object Contents extends OverStrategy
case class Pixels(threshold: Float) extends OverStrategy

/**
 * Since there is no cohesion among Libgdx primitives this class is needed.
 * This class should be extended to wrap visual primitives like a Texture, TextureRegion and BitmapFont
 */
trait Graphic[T <: AnyRef] {
  var primitive: T
  def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float = 0, centerY: Float = 0, width: Float = width, height: Float = height, scaleX: Float = 1.0f, scaleY: Float = 1.0f, rotation: Float = 0)

  def width: Float
  def height: Float

  def bounds: Rectangle[Float] = if (primitive == null) Rectangle.EMPTY_FLOAT else Rectangle(0f, 0f, width, height)
  def contents: Rectangle[Float] = if (primitive == null) Rectangle.EMPTY_FLOAT else bounds

  def isOver(p: Point2D[Float], strat: OverStrategy): Boolean = isOver(p.x, p.y, strat)
  def isOver(x: Float, y: Float, strat: OverStrategy): Boolean = {
    (primitive != null) && (0 <= x && x <= width && 0 <= y && y <= height)
  }
}

class Region(override var primitive: TextureRegion) extends Graphic[TextureRegion] {
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null) {
      spriteBatch.draw(primitive, x, y, centerX, centerY, width, height, scaleX, scaleY, rotation)
    }
  }

  override def width: Float = if (primitive != null) primitive.getRegionWidth else -1
  override def height: Float = if (primitive != null) primitive.getRegionHeight else -1

  override def isOver(x: Float, y: Float, strat: OverStrategy): Boolean = {
    if (primitive == null)
      return false

    strat match {
      case Bounds => (0 <= x && x <= width && 0 <= y && y <= height)
      case Contents => (0 <= x && x <= width && 0 <= y && y <= height)
      case Pixels(threshhold) =>
        if ((0 <= x && x <= width && 0 <= y && y <= height)) {
          val texX = primitive.getRegionX + x.round
          val texY = primitive.getRegionY + y.round
          val color = Graphic.PBOReadPixels(texX, texY, primitive.getTexture.getWidth, primitive.getTexture.getHeight, primitive.getTexture.getTextureObjectHandle)
          Color.rgba8888ToColor(Graphic.tempColor, color)
          Graphic.tempColor.a > threshhold
        } else {
          false
        }
    }
  }
}

class TextureAtlasRegion(override var primitive: TextureAtlas.AtlasRegion) extends Graphic[TextureAtlas.AtlasRegion] {
  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null) {
      spriteBatch.draw(primitive, x + (primitive.offsetX * scaleX), y + (primitive.offsetY * scaleY), centerX, centerY,
        primitive.packedWidth, primitive.packedHeight, scaleX, scaleY, rotation)
    }
  }

  override def width: Float = if (primitive != null) primitive.originalWidth else -1
  override def height: Float = if (primitive != null) primitive.originalHeight else -1

  override def contents: Rectangle[Float] = if (primitive == null) Rectangle.EMPTY_FLOAT else Rectangle(
    primitive.offsetX,
    primitive.offsetY,
    primitive.offsetX + primitive.packedWidth,
    primitive.offsetY + primitive.packedHeight)

  override def isOver(x: Float, y: Float, strat: OverStrategy): Boolean = {
    if (primitive == null)
      return false

    def insideContents: Boolean = {
      (primitive.offsetX <= x && x <= primitive.offsetX + primitive.packedWidth &&
        primitive.offsetY <= y && y <= primitive.offsetY + primitive.packedHeight)
    }

    strat match {
      case Bounds => (0 <= x && x <= width && 0 <= y && y <= height)
      case Contents => insideContents
      case Pixels(threshhold) =>
        if (insideContents) {
          val texX = primitive.getRegionX + x - primitive.offsetX
          val texY = primitive.getRegionY + y - primitive.offsetY
          val color = Graphic.PBOReadPixels(texX.round, texY.round, primitive.getTexture.getWidth, primitive.getTexture.getHeight, primitive.getTexture.getTextureObjectHandle)
          Color.rgba8888ToColor(Graphic.tempColor, color)
          Graphic.tempColor.a > threshhold
        } else {
          false
        }
    }
  }
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

  override def isOver(x: Float, y: Float, strat: OverStrategy): Boolean = {
    if (primitive == null)
      return false

    strat match {
      case Bounds => (0 <= x && x <= width && 0 <= y && y <= height)
      case Contents => (0 <= x && x <= width && 0 <= y && y <= height)
      case Pixels(threshhold) =>
        val texX = x.round
        val texY = y.round
        val color = Graphic.PBOReadPixels(texX, texY, primitive.getWidth, primitive.getHeight, primitive.getTextureObjectHandle)
        Color.rgba8888ToColor(Graphic.tempColor, color)
        Graphic.tempColor.a > threshhold
    }
  }
}

object Text {
  val FONT_CHARACTERS =
    """abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_¡!$%#@|\/¿?-+=()*&.;:,{}"´`'<>áéíóúüÁÉÍÓÚÜñÑ"""

  //  def getText(pixelSize: Int): TrueTypeText = {
  //    new TrueTypeText(getFont(pixelSize))
  //  }

  def getFont(pixelSize: Int): BitmapFont = {
    val font = TrueTypeFontFactory.createBitmapFont(
      Gdx.files.internal("com/belfrygames/starkengine/ubuntu.ttf"),
      FONT_CHARACTERS, 1024, 640, pixelSize, 1024, 640)

    font.setUseIntegerPositions(false)
    font
  }
}

trait Font extends Graphic[BitmapFont] {
  var text: String
}

class TrueTypeFont(val path: String, val pixelSize: Int, val charset: String = Text.FONT_CHARACTERS) extends Font {
  val bitmapFont = TrueTypeFontFactory.createBitmapFont(
    Gdx.files.internal(path),
    charset, 1024, 640, pixelSize, 1024, 640)

  bitmapFont.setUseIntegerPositions(false)

  def newFont(pixelSize: Int, charset: String = Text.FONT_CHARACTERS) = new TrueTypeFont(path, pixelSize, charset)

  private var textBounds: BitmapFont.TextBounds = null
  private[this] var currentText = ""
  text = currentText
  override def text_=(text: String) {
    if (currentText != text) {
      currentText = text
      textBounds = primitive.getBounds(text)
      textBounds = primitive.getMultiLineBounds(text)
    }
  }
  override def text = currentText

  override def primitive_=(value: BitmapFont) = {
    sys.error("Can't change the primitive of a TrueTypeFont")
  }
  override def primitive = bitmapFont

  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null) {
      primitive.setScale(scaleX, scaleY)
      val textx = x + centerX - scaleX * centerX
      val texty = y + centerY - scaleY * centerY + height * scaleY - primitive.getLineHeight
      primitive.drawMultiLine(spriteBatch, text, textx, texty)
    }
  }

  override def width: Float = {
    if (textBounds != null) {
      (textBounds.width / primitive.getScaleX)
    } else -1
  }

  override def height: Float = {
    if (primitive != null) {
      ((primitive.getLineHeight * text.lines.size) / primitive.getScaleY)
    } else -1
  }
}

class BitmapText(private var _primitive: BitmapFont) extends Font {
  var currentText: String = ""
  private var textBounds: BitmapFont.TextBounds = null

  primitive = _primitive

  override def primitive_=(value: BitmapFont) = {
    _primitive = value
    if (_primitive != null) {
      textBounds = _primitive.getBounds(text)
      textBounds = _primitive.getMultiLineBounds(text)
    } else {
      textBounds = null
    }
  }
  override def primitive = _primitive

  override def text_=(text: String) {
    if (currentText != text) {
      currentText = text
      textBounds = primitive.getBounds(text)
      textBounds = primitive.getMultiLineBounds(text)
    }
  }
  override def text = currentText

  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (_primitive != null) {
      _primitive.setScale(scaleX, scaleY)
      val textx = x + centerX - scaleX * centerX
      val texty = y + centerY - scaleY * centerY + height * scaleY
      _primitive.drawMultiLine(spriteBatch, text, textx, texty)
    }
  }

  override def width: Float = {
    if (textBounds != null) {
      (textBounds.width / _primitive.getScaleX)
    } else -1
  }

  override def height: Float = {
    if (_primitive != null) {
      ((_primitive.getLineHeight * text.lines.size) / _primitive.getScaleY)
    } else -1
  }
}

class TrueTypeText(private var _primitive: BitmapFont) extends Font {
  def this(font: TrueTypeFont) = this(font.bitmapFont)

  override var text: String = ""
  private var textBounds: BitmapFont.TextBounds = null

  primitive = _primitive

  override def primitive_=(value: BitmapFont) = {
    _primitive = value
    if (_primitive != null) {
      textBounds = _primitive.getBounds(text)
      textBounds = _primitive.getMultiLineBounds(text)
    } else {
      textBounds = null
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
    if (textBounds != null) {
      (textBounds.width / _primitive.getScaleX)
    } else -1
  }

  override def height: Float = {
    if (_primitive != null) {
      ((_primitive.getLineHeight * text.lines.size) / _primitive.getScaleY)
    } else -1
  }
}
