package com.belfrygames.starkengine.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import java.io.File

object Resources {
  def loadWithFileHandle(file : FileHandle, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    val texture = new Texture(file)
    if (width < 0 || height < 0) {
      new TextureRegion(texture, x, y, texture.getWidth, texture.getHeight)
    } else {
      new TextureRegion(texture, x, y, width, height)
    }
  }
  
  def split(file : FileHandle, width: Int, height: Int, margin: Int, spacing: Int, flipX: Boolean, flipY: Boolean) : Array[Array[TextureRegion]] = {
    def indexToPos(x: Int, y: Int) : Tuple2[Int, Int] = {
        (margin + (width + spacing) * x, margin + (height + spacing) * y)
    }
    
    val texture = new Texture(file)
    
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
  
  def split(name: String, width: Int, height: Int, margin: Int, spacing: Int, flipX: Boolean, flipY: Boolean) : Array[Array[TextureRegion]] = {
    Resources.split(Gdx.files.internal(name), width, height, margin, spacing, flipX, flipY)
  }
  
  def loadWithFile(file : File, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    Resources.loadWithFileHandle(new FileHandle(file), width, height, x, y)
  }
  
  def load(name : String, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    Resources.loadWithFileHandle(Gdx.files.internal(name), width, height, x, y)
  }
}

trait Resources {
  def initialize()
  
  val map = collection.mutable.Map[String, TextureRegion]()
  def set(id: String, texture: TextureRegion) {
    map.put(id, texture)
  }
  
  def get(id: String) = map(id)
}
