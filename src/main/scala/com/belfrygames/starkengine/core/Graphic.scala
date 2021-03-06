package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.{ Texture, Color }
import com.badlogic.gdx.graphics.g2d.{ BitmapFont, SpriteBatch, TextureRegion, TextureAtlas }
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.Gdx
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.{ GLContext, ARBPixelBufferObject, ARBBufferObject, GL11 }
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

  lazy val SQUARE: Graphic[_] = createRectangle(1, 1)

  def createRectangle(width: Int, height: Int, color: Color = Color.WHITE): Graphic[_] = {
    import com.badlogic.gdx.graphics.{ Texture, Pixmap }
    import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap
    val pix = new Gdx2DPixmap(width, height, Gdx2DPixmap.GDX2D_FORMAT_RGBA8888)
    pix.clear(Color.rgba8888(color))
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

sealed trait OverBehavior
case object All extends OverBehavior
case object OnlyEnabled extends OverBehavior

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

object Font {
  private var cachedGenerator = Map[String, FreeTypeFontGenerator]()

  def getBitMapFont(file: FileHandle, size: Int): BitmapFont = {
    val path = file.path
    if (!cachedGenerator.contains(path)) {
      cachedGenerator += (path -> new FreeTypeFontGenerator(file))
    }

    val bitmapFont = cachedGenerator(path).generateFont(size)
    bitmapFont.setUseIntegerPositions(false)
    bitmapFont
  }

  def clearCache() {
    cachedGenerator.values.foreach(_.dispose())
    cachedGenerator = Map()
  }
}

trait Font extends Graphic[BitmapFont] {
  def newFont(pixelSize: Int, charset: String = FreeTypeFontGenerator.DEFAULT_CHARS): Font

  protected final def updateBounds() {
    if (primitive != null) {
      val textBounds = primitive.getMultiLineBounds(text)
      textWidth = textBounds.width
      textHeight = textBounds.height
    } else {
      textWidth = 0
      textHeight = 0
    }
  }

  private[this] var textWidth = 0f
  private[this] var textHeight = 0f

  override def width: Float = textWidth
  override def height: Float = textHeight

  private[this] var _currentText = ""
  def text = _currentText
  def text_=(text: String) {
    if (_currentText != text) {
      _currentText = text
      updateBounds()
    }
  }

  override def draw(spriteBatch: SpriteBatch, x: Float, y: Float, centerX: Float, centerY: Float, width: Float, height: Float, scaleX: Float, scaleY: Float, rotation: Float) {
    if (primitive != null) {
      primitive.setScale(scaleX, scaleY)
      val textx = x + centerX - scaleX * centerX
      val texty = y + centerY - scaleY * centerY + height * scaleY
      primitive.drawMultiLine(spriteBatch, text, textx, texty)
    }
  }
}

class TrueTypeFont(val path: String, val pixelSize: Int, val charset: String = FreeTypeFontGenerator.DEFAULT_CHARS) extends Font {
  private val _primitive = Font.getBitMapFont(Gdx.files.internal(path), pixelSize)
  updateBounds()

  override def primitive = _primitive
  override def primitive_=(value: BitmapFont) = sys.error("Can't change the primitive of a TrueTypeFont")

  override def newFont(pixelSize: Int, charset: String = FreeTypeFontGenerator.DEFAULT_CHARS) = {
    new TrueTypeFont(path, pixelSize, charset)
  }
}

class BitmapText(private[this] var _primitive: BitmapFont) extends Font {
  updateBounds()

  override def primitive = _primitive
  override def primitive_=(value: BitmapFont) = {
    _primitive = value
    updateBounds()
  }

  override def newFont(pixelSize: Int, charset: String = FreeTypeFontGenerator.DEFAULT_CHARS) = {
    new BitmapText(Font.getBitMapFont(_primitive.getData().getFontFile(), pixelSize))
  }
}
