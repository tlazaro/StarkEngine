package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.Color

case class Style(font: Font, color: Color) {
  def withFont(font: Font) = Style(font, color.cpy)
  def withSize(size: Int) = Style(font.newFont(size), color.cpy)
  def withColor(color: Color) = Style(font, color)
}

class Label(private[this] var text0: String, private[this] var style0: Style) extends Sprite(style0.font) {
  private var bounds: BitmapFont.TextBounds = null
  private def updateBounds() {
    bounds = style.font.primitive.getBounds(text0)
    bounds = style.font.primitive.getMultiLineBounds(text0)
  }

  def text = text0
  def text_=(text: String) = {
    text0 = text
    updateBounds()
  }
  updateBounds()
  
  override def color = style.color
  override def color_=(color: Color) {
    style = style.withColor(color)
    updateBounds()
  }
  
  def style = style0
  def style_=(style: Style) = {
    style0 = style
    graphic = style0.font
    updateBounds()
  }

  override protected def setDrawingColor(spriteBatch: SpriteBatch, color: Color) {
    super.setDrawingColor(spriteBatch, color)
    if (style.font.primitive != null) {
      style.font.primitive.setColor(color)
      style.font.text = text
    }
  }

  override def width: Float = {
    if (bounds != null) {
      (bounds.width / scaleX)
    } else -1
  }

  override def height: Float = {
    if (style.font.primitive != null) {
      ((style.font.primitive.getLineHeight * text.lines.size) / scaleY)
    } else -1
  }
}
