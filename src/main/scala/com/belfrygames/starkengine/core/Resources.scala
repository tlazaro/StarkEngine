package com.belfrygames.starkengine.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.TextureLoader
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import java.io.File
import scala.io.Source

object Resources {
  def loadWithFileHandle(file : FileHandle, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    val texture = new Texture(file)
    if (width < 0 || height < 0) {
      new TextureRegion(texture, x, y, texture.getWidth, texture.getHeight)
    } else {
      new TextureRegion(texture, x, y, width, height)
    }
  }
  
  def split(texture : Texture, width: Int, height: Int, margin: Int, spacing: Int, flipX: Boolean, flipY: Boolean) : Array[Array[TextureRegion]] = {
    def indexToPos(x: Int, y: Int) : Tuple2[Int, Int] = {
      (margin + (width + spacing) * x, margin + (height + spacing) * y)
    }

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
    val texture = getTexture(name)
    Resources.split(texture, width, height, margin, spacing, flipX, flipY)
  }
  
  def loadWithFile(file : File, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    Resources.loadWithFileHandle(new FileHandle(file), width, height, x, y)
  }
  
  private def getTexture(name: String) = {
    println("OPENGL: " + Gdx.gl)
    println("GLU: " + Gdx.glu)
    if (!assets.isLoaded(name)) {
      assets.load(name, classOf[Texture])
      assets.finishLoading()
    }
    assets.get(name, classOf[Texture])
  }
  
  def load(name : String, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    val texture = getTexture(name)
    if (width < 0 || height < 0) {
      new TextureRegion(texture, x, y, texture.getWidth, texture.getHeight)
    } else {
      new TextureRegion(texture, x, y, width, height)
    }
  }
  
  def loadFile(file: String) = getAsset[String](file)
  
  private def getAsset[T:Manifest](name: String): T = {
    if (!assets.isLoaded(name)) {
      assets.load(name, manifest[T].erasure)
      assets.finishLoading()
    }
    assets.get(name, manifest[T].erasure).asInstanceOf[T]
  }
  
  val assets = new AssetManager
  assets.setLoader(classOf[Texture], new TextureLoader(new InternalFileHandleResolver()))
  assets.setLoader(classOf[String], new TextLoader(new InternalFileHandleResolver()))
  
  class TextParameter extends AssetLoaderParameters[String]
  class TextLoader(resolver: FileHandleResolver) extends SynchronousAssetLoader[String, TextParameter](resolver) {
    override def load(manager: AssetManager, fileName: String, parameter: TextParameter): String = {
      Source.fromInputStream(resolve(fileName).read).mkString
    }
    
    override def getDependencies(fileName: String, parameter: TextParameter): com.badlogic.gdx.utils.Array[AssetDescriptor[_]] = {
      null
    }
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
