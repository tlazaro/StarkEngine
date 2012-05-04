package com.belfrygames.starkengine.map

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.belfrygames.starkengine.core.Node

class Layer(val name: String, private var cols: Int, private var rows: Int, val tileWidth: Int, val tileHeight: Int, var tileSet: TileSet) extends Node {
  private var tiles = Array.ofDim[Int](cols, rows)
  
  def apply(x: Int, y: Int): Tile = {
    if (tileSet == null)  {
      null
    } else {
      tileSet(tiles(y)(x))
    }
  }
  
  def valueAt(x: Int, y: Int): Int = {
    tiles(y)(x)
  }
  
  def update(x: Int, y: Int, tile: Int) {
    tiles(y)(x) = tile
  }
  
  def resize(cols: Int, rows: Int) {
    
  }
  
  def fill(tileSet: TileSet) {
    this.tileSet = tileSet
    for(y <- 0 until rows; x <- 0 until cols) {
      this(x, y) = 0
    }
  }
  
  def getWidth = cols
  def getHeight = rows
  
  override def draw(spriteBatch: SpriteBatch) {
    if (tileSet != null) {
      for(y <- 0 until tiles.length; x <- 0 until tiles(0).length; tile = this(x, y); if tile != null) {
        tile.draw(spriteBatch, x * tileWidth, (tiles.length - 1 - y) * tileHeight)
      }
    }
    super.draw(spriteBatch)
  }
}
