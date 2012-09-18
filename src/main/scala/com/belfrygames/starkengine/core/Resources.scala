package com.belfrygames.starkengine.core

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.TextureLoader
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.Gdx
import java.io.File
import scala.io.Source
import java.io.FileNotFoundException

object Resources {
  def loadWithFileHandle(file : FileHandle, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    val texture = new Texture(file)
    if (width < 0 || height < 0) {
      new TextureRegion(texture, x, y, texture.getWidth, texture.getHeight)
    } else {
      new TextureRegion(texture, x, y, width, height)
    }
  }
  
  def split(texture : Texture, width: Int, height: Int, margin: Int, spacing: Int, flipX: Boolean, flipY: Boolean) : Array[TextureRegion] = {
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
    
    val res = Array.ofDim[TextureRegion](ySlices * xSlices)
    for (x <- 0 until xSlices; y <- 0 until ySlices) {
      val coords = indexToPos(x, y)
      res(y * xSlices + x) = new TextureRegion(texture, coords._1, coords._2, width, height)
      res(y * xSlices + x).flip(flipX, flipY)
    }
    
    res
  }
  
  def split(texture : TextureRegion, width: Int, height: Int, margin: Int, spacing: Int, flipX: Boolean, flipY: Boolean) : Array[TextureRegion] = {
    def indexToPos(x: Int, y: Int) : Tuple2[Int, Int] = {
      (margin + (width + spacing) * x, margin + (height + spacing) * y)
    }

    val xSlices = texture.getRegionWidth match {
      case n if n >= margin * 2 + width => 1 + (n - margin * 2 - width) / (width + spacing)
      case _ => 0
    }
    val ySlices = texture.getRegionHeight match {
      case n if n >= margin * 2 + height => 1 + (n - margin * 2 - height) / (height + spacing)
      case _ => 0
    }
    
    val res = Array.ofDim[TextureRegion](ySlices * xSlices)
    for (x <- 0 until xSlices; y <- 0 until ySlices) {
      val coords = indexToPos(x, y)
      res(y * xSlices + x) = new TextureRegion(texture, coords._1, coords._2, width, height)
      res(y * xSlices + x).flip(flipX, flipY)
    }
    
    res
  }
  
  def split(name: String, width: Int, height: Int, margin: Int, spacing: Int, flipX: Boolean, flipY: Boolean) : Array[TextureRegion] = {
    val texture = load(name)
    Resources.split(texture, width, height, margin, spacing, flipX, flipY)
  }
  
  def loadWithFile(file : File, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    Resources.loadWithFileHandle(new FileHandle(file), width, height, x, y)
  }
  
  def loadAnim(name : String) : Either[Throwable, Seq[TextureAtlas.AtlasRegion]] = {
    import scala.collection.JavaConversions._
    
    val path = name.split("/")
    atlases.get(path.head) match {
      case Some(atlas) =>
        val name = path.tail.mkString("/")
        Right(atlas.findRegions(name).iterator.toSeq.map(_.asInstanceOf[TextureAtlas.AtlasRegion]))
      case _ => Left(new FileNotFoundException("Could not find: " + name))
    }
  }
  
  def load(name : String, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    val path = name.split("/")
    atlases.get(path.head) match {
      case Some(atlas) => {
          atlas.findRegion(path.drop(1).mkString("/"))
        }
      case _ => {
          val texture = getAsset[Texture](name)
          if (width < 0 || height < 0) {
            new TextureRegion(texture, x, y, texture.getWidth, texture.getHeight)
          } else {
            new TextureRegion(texture, x, y, width, height)
          }
        }
    }
  }
  
  def loadFile(file: String) = getAsset[String](file)
  
  def getAsset[T:Manifest](name: String): T = {
    if (!assets.isLoaded(name)) {
      assets.load(name, manifest[T].erasure)
      assets.finishLoading()
    }
    assets.get(name, manifest[T].erasure).asInstanceOf[T]
  }
  
  val assets = new AssetManager
  assets.setLoader(classOf[Texture], new TextureLoader(new InternalFileHandleResolver()))
  assets.setLoader(classOf[String], new TextLoader(new InternalFileHandleResolver()))
  assets.setLoader(classOf[TextureAtlas], new TextureAtlasLoader(new InternalFileHandleResolver()))
  
  var atlases = Map[String, TextureAtlas]()
  
  def loadTexturePack(packName: String) = {
    val atlas = getAsset[TextureAtlas](packName)
    atlases += (packName.split("\\.").head -> atlas)
    atlas
  }
  
  class OpenBuildResolver extends FileHandleResolver {
    override def resolve(fileName: String): FileHandle = {
      val res = Gdx.files.external(fileName)
      if (res.exists) res else Gdx.files.internal(fileName)
    }
  }

  class TextParameter extends AssetLoaderParameters[String]
  class TextLoader(resolver: FileHandleResolver) extends SynchronousAssetLoader[String, TextParameter](resolver) {
    override def load(manager: AssetManager, fileName: String, parameter: TextParameter): String = {
      Source.fromInputStream(resolve(fileName).read).mkString
    }
    
    override def getDependencies(fileName: String, parameter: TextParameter): com.badlogic.gdx.utils.Array[AssetDescriptor[_]] = {
      null
    }
  }
  
  class TextureAtlasParameter extends AssetLoaderParameters[TextureAtlas]
  class TextureAtlasLoader(resolver: FileHandleResolver) extends SynchronousAssetLoader[TextureAtlas, TextureAtlasParameter](resolver) {
    override def load(manager: AssetManager, fileName: String, parameter: TextureAtlasParameter): TextureAtlas = {
      new TextureAtlas(resolve(fileName))
    }
    
    override def getDependencies(fileName: String, parameter: TextureAtlasParameter): com.badlogic.gdx.utils.Array[AssetDescriptor[_]] = {
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
