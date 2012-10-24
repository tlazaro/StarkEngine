package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

case class Style(font: Font, color: Color) {
  def withFont(font: Font) = Style(font, color.cpy)
  def withSize(size: Int) = Style(font.newFont(size), color.cpy)
  def withColor(color: Color) = Style(font, color)
}

class Label(private[this] var text0: String, private[this] var style0: Style) extends Sprite(style0.font) {
  private var _width = -1f
  private var _height = -1f

  updateBounds()

  private def updateBounds() {
    val old = style.font.text
    style.font.text = text
    _width = style.font.width
    _height = style.font.height
    style.font.text = old
  }

  def text = text0
  def text_=(text: String) = {
    text0 = text
    updateBounds()
  }

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

  override def width: Float = _width
  override def height: Float = _height
  
  override protected def bounds(renderer: ShapeRenderer) {
    if (graphic != null) {
      if (selected) {
        renderer.setColor(1f, 1f, 0f, 1f)
      } else {
        renderer.setColor(0f, 1f, 0f, 1f)
      }
      val old = style.font.text
      style.font.text = text
      drawRect(renderer, graphic.bounds)
      style.font.text = old
    }
  }
  
  override protected def contents(renderer: ShapeRenderer) {
    if (graphic != null) {
      renderer.setColor(1f, 0f, 0f, 1f)
      val old = style.font.text
      style.font.text = text
      drawRect(renderer, graphic.contents)
      style.font.text = old
    }
  }
}
