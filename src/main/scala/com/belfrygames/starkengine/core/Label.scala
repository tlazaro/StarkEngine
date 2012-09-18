package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.Color

class Label(val font: Graphic[BitmapFont]) extends Sprite(font) {
  override protected def setDrawingColor(spriteBatch: SpriteBatch, color: Color) {
    super.setDrawingColor(spriteBatch, color)
    if (font.primitive != null) {
      font.primitive.setColor(color)
    }
  }
}
