package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.Color

class Label(private[this] var text0: String, val font: Font) extends Sprite(font) {
  private var bounds: BitmapFont.TextBounds = null
  private def updateBounds() {
    bounds = font.primitive.getBounds(text0)
    bounds = font.primitive.getMultiLineBounds(text0)
  }

  def text = text0
  def text_=(text: String) = {
    text0 = text
    updateBounds()
  }
  updateBounds()

  override protected def setDrawingColor(spriteBatch: SpriteBatch, color: Color) {
    super.setDrawingColor(spriteBatch, color)
    if (font.primitive != null) {
      font.primitive.setColor(color)
      font.text = text
    }
  }

  override def width: Float = {
    if (bounds != null) {
      (bounds.width / scaleX)
    } else -1
  }

  override def height: Float = {
    if (font.primitive != null) {
      ((font.primitive.getLineHeight * text.lines.size) / scaleY)
    } else -1
  }
}
