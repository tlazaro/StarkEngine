package com.belfrygames.starkengine.map

import com.belfrygames.starkengine.core.Resources

object TileSet {
  trait FileType
  case object RGB extends FileType
  case object ARGB extends FileType
  
  def buildTileSet(update: String): TileSet = {
    buildTileSet(JSON.parseText(update))
  }
  
  def buildTileSet(update: JSON.ParseResult[Any]): TileSet = {
    val isMap: PartialFunction[Any, Map[String, Any]] = {case n: Map[String, Any] => n}
    val isNumber: PartialFunction[Any, Double] = {case n: Double => n}
    val isString: PartialFunction[Any, String] = {case n: String => n}
    val isList: PartialFunction[Any, List[Any]] = {case n: List[Any] => n}
    val isObjectList: PartialFunction[Any, List[Map[String, Any]]] = {case n: List[Map[String, Any]] => n}
    
    def extractNumber(op: Option[Any], default: Int): Int = op match {
      case Some(n: Double) => n.toInt
      case _ => default
    } 
    
    var result: TileSet = null
    update match {
      case p: JSON.Success[Any] => {
          p.get match {
            case obj : Map[String, Any] => {
                for(map <- obj.get("tileset").collect(isMap);
                    file <- map.get("file").collect(isString);
                    width <- map.get("width").collect(isNumber);
                    height <- map.get("height").collect(isNumber);
                    tiles <- map.get("tiles").collect(isObjectList)) {
                  
                  val margin = extractNumber(map.get("margin"), 0)
                  val spacing = extractNumber(map.get("spacing"), 0)
                  val offsetX = extractNumber(map.get("offsetX"), 0)
                  val offsetY = extractNumber(map.get("offsetY"), 0)
                  
                  val tileList = for(tile <- tiles;
                                     name <- tile.get("name").collect(isString);
                                     moveCost <- tile.get("moveCost").collect(isNumber);
                                     defense <- tile.get("defense").collect(isNumber)) yield {
                  
                    new Tile(null, name, moveCost.toInt, defense.toInt)
                  }
                  
                  result = new TileSet(file, TileSet.ARGB, tileList.toIndexedSeq, width.toInt, height.toInt, margin,
                                       spacing, offsetX, offsetY)
                }
              }
            case _ => {
                println("INVALID JSON UPDATE CANT FIND 'tileset'")
              }
          }
        }
      case _ => {
          println("INVALID JSON UPDATE")
        }
    }
    
    result
  }
}

class TileSet(val file: String, // Path hacia el archivo de imagen, relativo o absoluto
              val fileType: TileSet.FileType, // ARGB por defecto IMPLICITO? en el caso de algunos tiles no es necesario el canal alpha,
              val tiles: IndexedSeq[Tile], // List of tiles
              val width: Int, // n > 0, ancho en pixeles de cada sprite
              val height: Int, // n > 0, alto en pixeles de cada sprite
              val margin: Int = 0, // n >= 0, 0 por defecto. OPCIONAL
              val spacing: Int = 0, // n >=0, 0 por defecto. OPCIONAL
              val offsetX: Int = 0, // z, número entero. 0 por defecto. OPCIONAL
              val offsetY: Int = 0 // z, número entero. 0 por defecto. OPCIONAL
) {
  var loadedTiles = false
  /** Processes the file and assigns the texture to the defined tiles */
  def reloadTiles() {
    val regs = Resources.split(file, width, height, margin, spacing, false, false)
    for ((tile, reg) <- tiles.zip(regs)) {
      tile.primitive = reg
    }
    loadedTiles = true
  }
  
  def size() = tiles.size
  
  def apply(id: Int) = {
    if (id < 0) null else tiles(id)
  }
}