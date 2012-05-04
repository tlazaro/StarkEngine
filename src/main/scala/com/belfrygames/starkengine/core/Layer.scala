package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class Layer(val cam: Camera) extends Node {
  override def draw(spriteBatch: SpriteBatch) {
    spriteBatch.end()
    val m = spriteBatch.getProjectionMatrix.cpy
    spriteBatch.setProjectionMatrix(cam.combined)
    spriteBatch.begin()
    super.draw(spriteBatch)
    spriteBatch.end()
    spriteBatch.setProjectionMatrix(m)
    spriteBatch.begin()
  }
  
  override def debugDraw(renderer: ShapeRenderer) {
    renderer.setProjectionMatrix(cam.combined.cpy)
    super.debugDraw(renderer)
  }
}
