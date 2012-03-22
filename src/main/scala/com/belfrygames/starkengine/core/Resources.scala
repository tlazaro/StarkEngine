package com.belfrygames.starkengine.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import java.io.File

trait Resources {
  def initialize()
  
  def loadWithFile(file : File, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    loadWithFileHandle(new FileHandle(file), width, height, x, y)
  }
  
  def load(name : String, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    loadWithFileHandle(Gdx.files.internal(name), width, height, x, y)
  }
  
  def loadWithFileHandle(file : FileHandle, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    Gdx.app.log("Resources", "Loading: " + file)
                
    val texture = new Texture(file)
    if (width < 0 || height < 0) {
      new TextureRegion(texture, x, y, texture.getWidth, texture.getHeight)
    } else {
      new TextureRegion(texture, x, y, width, height)
    }
  }
  
  def split(name: String, width: Int, height: Int, margin: Int, spacing: Int, flipX: Boolean, flipY: Boolean) : Array[Array[TextureRegion]] = {
    def indexToPos(x: Int, y: Int) : Tuple2[Int, Int] = {
        (margin + (width + spacing) * x, margin + (height + spacing) * y)
    }
    
    Gdx.app.log("Resources", "Splitting: " + name)
    
    val texture = new Texture(Gdx.files.internal(name))
    
    val xSlices = texture.getWidth match {
      case n if n >= margin * 2 + width => 1 + (n - margin * 2 - width) / (width + spacing)
      case _ => 0
    }
    val ySlices = texture.getHeight match {
      case n if n >= margin * 2 + height => 1 + (n - margin * 2 - height) / (height + spacing)
      case _ => 0
    }
    
    val res = Array.ofDim[TextureRegion](ySlices, xSlices)
    for (x <- 0 until xSlices; y <- 0 until ySlices) {
      val coords = indexToPos(x, y)
      res(y)(x) = new TextureRegion(texture, coords._1, coords._2, width, height)
      res(y)(x).flip(flipX, flipY)
    }
    
    res
  }
  
  val map = collection.mutable.Map[String, TextureRegion]()
  def set(id: String, texture: TextureRegion) {
    map.put(id, texture)
  }
  
  def get(id: String) = map(id)
}
