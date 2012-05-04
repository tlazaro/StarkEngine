package com.belfrygames.starkengine.map

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.belfrygames.starkengine.core.{Node, Resources}
import com.belfrygames.starkengine.tags._
import java.awt.datatransfer.{DataFlavor, Transferable, UnsupportedFlavorException}
import java.io.IOException
import javax.swing.{JComponent, JTable, TransferHandler}
import javax.swing.event.{TableModelEvent, TableModelListener}
import javax.swing.table.TableModel
import scala.collection.mutable.ListBuffer

object StarkMap {
  val COLUMN_NAMES = Array[String]("Visible", "Name")
  val COLUMN_CLASSES = Array[Class[_]](classOf[java.lang.Boolean], classOf[String])
  
  def buildNewMap(update: String, starkMap: StarkMap = null): StarkMap = {
    buildMap(JSON.parseText(update), starkMap)
  }
  
  def buildMap(update: JSON.ParseResult[Any], starkMap: StarkMap = null): StarkMap = {
    val isMap: PartialFunction[Any, Map[String, Any]] = {case n: Map[String, Any] => n}
    val isNumber: PartialFunction[Any, Double] = {case n: Double => n}
    val isString: PartialFunction[Any, String] = {case n: String => n}
    
    var result: StarkMap = starkMap
    update match {
      case p: JSON.Success[Any] => {
          p.get match {
            case obj : Map[String, Any] => {
                for(map <- obj.get("map").collect(isMap);
                    layersDef <- map.get("layers").collect(isMap);
                    w <- map.get("width").collect(isNumber);
                    h <- map.get("height").collect(isNumber);
                    tWidth <- map.get("tileWidth").collect(isNumber);
                    tHeight <- map.get("tileHeight").collect(isNumber);
                    tileSetName <- map.get("tileSet").collect(isString)) {
                  
                  if (result == null) {
                    result = new StarkMap(w.toInt, h.toInt, tWidth.toInt, tHeight.toInt)
                  } else {
                    result.cols = w.toInt
                    result.rows = h.toInt
                    result.tileWidth = tWidth.toInt
                    result.tileHeight = tHeight.toInt
                  }
                
                  result.tileSetName = tileSetName
                  result.tileSet = TileSet.fromSplitTexture(
                    Resources.split(tileSetName, result.tileWidth, result.tileHeight, 1, 2, false, false))
                
                  result.clearLayers()
                  for((key, value) <- layersDef; list = value.asInstanceOf[List[List[Double]]]) {
                    val layer = result.addLayer(key)
                    for(y <- 0 until list.size; x <- 0 until list.head.size) {
                      layer(x, y) = list(y)(x).toInt
                      layer.tileSet = result.tileSet
                    }
                  }
                }
              }
            case _ => {
                println("INVALID JSON UPDATE CANT FIND 'map'")
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

class StarkMap(private var width0: Int,
               private var height0: Int,
               var tileWidth: Int,
               var tileHeight: Int) extends Node with TableModel {
  private var tileSet: TileSet = null
  private var tileSetName: String = null
  
  def getTileSet = tileSet
  def getTileSetName() = tileSetName
  
  private var listener: Option[MapListener] = None
  private val listeners = new java.util.ArrayList[TableModelListener]()
  
  def clearListener() = listener = None
  def setListener(listener: MapListener) {
    this.listener = Some(listener)
    fireMapChanged()
  }
  
  def cols = width0
  def cols_=(value: Int) {
    width0 = value
  }
    
  def rows: Int = height0
  def rows_=(value: Int) {
    height0 = value
  }
  
  override def width = cols * tileWidth
  override def height = rows * tileHeight
  
  var layers = new ListBuffer[Layer]()
  
  override def update(elapsed : Long @@ Milliseconds) {
    super.update(elapsed)
    
    if (tileSet == null) {
      tileSet = TileSet.fromSplitTexture(Resources.split(tileSetName, tileWidth, tileHeight, 1, 2, false, false))
      for(layer <- layers) {
        layer.tileSet = tileSet
      }
    }
  }
  
  def applyUpdate(update: JSON.ParseResult[Any]) {
    StarkMap.buildMap(update, this)
    fireTableChanged()
  }
  
  def serializeText(): String = {
    var ind = 0
    def indent = " " * (ind * 2)
    def indentValue(value: Int) = " " * (value * 2)
    
    val b = new StringBuilder
    b append "{\n"; ind += 1
    b append indent + "\"map\": {\n"; ind += 1
    
    b append indent + "\"width\": " + width + ",\n"
    b append indent + "\"height\": " + height + ",\n"
    b append indent + "\"tileWidth\": " + tileWidth + ",\n"
    b append indent + "\"tileHeight\": " + tileHeight + ",\n"
    b append indent + "\"tileSet\": " + "\"com/belfrygames/mapeditor/terrenos.png\"" + ",\n"
    
    b append indent + "\"layers\": {\n"; ind += 1
    for(layer <- layers) {
      b append indent + "\"" + layer.name + "\": "; ind += 1
      
      b append (for(y <- 0 until layer.getHeight) yield {
          (for(x <- 0 until layer.getWidth) yield {
              layer.valueAt(x, y)
            }).mkString(indent + "[", ", ", "]")
        }).mkString("[\n", ",\n", "\n" + indentValue(ind - 1) + "],\n")
      ind -= 1
    }
    
    if (!layers.isEmpty)
      b.deleteCharAt(b.length - 2)
    
    while(ind > 0) {
      ind -= 1
      b append indent + "}\n"
    }
    
    b.toString
  }
  
  def clearLayers() = {
    for (layer <- layers) {
      remove(layer)
    }
    layers.clear()
  }
  
  def addLayer(layer: Layer, at: Int): Layer = {
    if (at < 0 || at >= layers.size) {
      layers.append(layer)
    } else {
      layers.insert(at, layer)
    }
    
    add(layer, layer.name)
    layer
  }
  
  def addLayer(name: String, at: Int = -1): Layer = {
    val layer = new Layer(name, cols, rows, tileWidth, tileHeight, null)
    addLayer(layer, at)
  }
  
  def organizeLayers(names: Array[String], visible: Array[Boolean]): Boolean = {
    if (names.length != visible.length || names.length != layers.size) {
      println("Wrong names or visible sizes")
      return false
    }
    
    val mapped = names.map(name => layers.find(_.name == name)).flatten
    if (mapped.length != layers.size) {
      println("Layers not found")
      return false
    }
    
    clearLayers()
    mapped.foreach(addLayer(_, -1))
    
    for(i <- 0 until visible.length) {
      setVisible(i, visible(i))
    }
    
    fireMapChanged()
    true
  }
  
  def setVisible(index: Int, visible: Boolean) {
    layers(index).visible = visible
  }
  
  def removeLayer(at: Int): Layer = {
    val layer = layers.remove(at)
    remove(layer)
    layer
  }
  
  def removeLayer(layer: Layer): Layer = {
    removeLayer(layers indexOf layer)
  }
  
  def layerNames(): Array[String] = layers.map(_.name).toArray
  def layerVisible(): Array[Boolean] = layers.map(_.visible).toArray
  
  def fromTexture(regions: Array[Array[TextureRegion]]) {
    val tiles = TileSet.fromSplitTexture(regions)
    val layer = addLayer("background", 0)
    layer.fill(tiles)
  }
  
  var currentLayer = -1
  def getCurrentLayer: Layer = if (currentLayer < 0) layers.last else layers(currentLayer)
  def setCurrentLayer(index: Int) {
    if (index < 0 || index >= layers.size) {
      // ignore
      println("Invalid Layer index: " + index)
    }
    currentLayer = index
  }
  
  var currentTileId = -1
  def getCurrentTileId: Int = currentTileId
  def setCurrentTileId(currentTileId: Int) {
    this.currentTileId = currentTileId
  }
  
  var currentTool : Tool = Selection
  def getCurrentTool: Tool = {
    currentTool
  }
  def setCurrentTool(currentTool: Tool) {
    this.currentTool = currentTool
  }
  
  def applyTool(x: Float, y: Float, tool: Tool): Boolean = {
    var change = false
    if ((0 <= x && x < tileWidth * cols) && (0 <= y && y < tileHeight * rows)) {
      val xCoord = (x / tileWidth).toInt
      val yCoord = rows - 1 - (y / tileHeight).toInt
    
      tool match {
        case Brush => {
            val old = getCurrentLayer.valueAt(xCoord, yCoord)
            if (getCurrentTileId != old) {
              getCurrentLayer(xCoord, yCoord) = getCurrentTileId
              change = true
            }
          }
        case BucketFill => {
            val old = getCurrentLayer.valueAt(xCoord, yCoord)
            // TODO implement flood fill algorithm
            if (getCurrentTileId != old) {
              getCurrentLayer(xCoord, yCoord) = getCurrentTileId
              change = true
            }
          }
        case Eraser => {
            val old = getCurrentLayer.valueAt(xCoord, yCoord)
            if (old != -1) {
              getCurrentLayer(xCoord, yCoord) = -1
              change = true
            }
          }
        case Selection => {
          }
      }
    }
    if (change) fireMapChanged()
    change
  }
  
  override def debugDraw(renderer: ShapeRenderer) {
    super.debugDraw(renderer)
    
    renderer.setColor(1f, 1f, 1f, 1f)
    renderer.begin(ShapeType.Line)
    renderer.identity()
    for(x <- Range(0, cols * tileWidth, tileWidth).inclusive) {
      renderer.line(x, 0, x, rows * tileHeight)
    }
    for(y <- Range(0, rows * tileHeight, tileHeight).inclusive) {
      renderer.line(0, y, cols * tileWidth, y)
    }
    renderer.end()
  }
  
  private def resizeLayers() {
    for(layer <- layers) {
      layer.resize(cols, rows)
    }
  }
  
  private def fireMapChanged() {
    listener foreach (_.mapChanged(this))
  }
  
  override def getRowCount(): Int = layers.size
  
  override def getColumnCount(): Int = StarkMap.COLUMN_NAMES.length

  override def getColumnName(columnIndex: Int): String = StarkMap.COLUMN_NAMES(columnIndex)

  override def getColumnClass(columnIndex: Int): Class[_] = StarkMap.COLUMN_CLASSES(columnIndex)

  override def isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = {
    columnIndex match {
      case 0 => true
      case _ => false
    }
  }

  override def getValueAt(rowIndex: Int, columnIndex: Int): AnyRef = {
    columnIndex match {
      case 0 => if (layers(layers.size - 1 - rowIndex).visible) java.lang.Boolean.TRUE else java.lang.Boolean.FALSE
      case 1 => layers(layers.size - 1 - rowIndex).name
      case _ => null
    }
  }
  
  private def getValueAt(rowIndex: Int): (Boolean, String) = {
    val layer = layers(layers.size - 1 - rowIndex)
    (layer.visible, layer.name)
  }

  override def setValueAt(value: AnyRef, rowIndex: Int, columnIndex: Int) {
    columnIndex match {
      case 0 => layers(layers.size - 1 - rowIndex).visible = value.asInstanceOf[java.lang.Boolean]
      case col => println("READ ONLY: " + col)
    }
  }
  
  override def addTableModelListener(tl: TableModelListener) {
    listeners.add(tl)
  }

  override def removeTableModelListener(tl: TableModelListener) {
    listeners.remove(tl)
  }
  
  def selectionChanged(start: Int, end: Int) {
    setCurrentLayer(layers.size - 1 - end)
  }
  
  def moveRows(rowIndex: Int, selection: EntrySelection) {
    import scala.collection.JavaConversions._
    
    var row = layers.size - rowIndex
    
    val moved = new ListBuffer[Layer]()
    for (entry <- selection.entries) {
      val index = layers.indexWhere(_.name == entry.name)
      moved.prepend(removeLayer(index))
      if (index < row) {
        row -= 1
      }
    }
    
    val (start, end) = layers.splitAt(row)
    clearLayers()
    start.foreach(addLayer(_, -1))
    moved.foreach(addLayer(_, -1))
    end.foreach(addLayer(_, -1))
    
    fireTableChanged()
  }
  
  private def fireTableChanged() {
    import scala.collection.JavaConversions._
    val event = new TableModelEvent(this)
    listeners.foreach(_.tableChanged(event))
  }
  
  class LayersTransferHandler(name: String) extends TransferHandler(name) {

    override def getSourceActions(c: JComponent) = TransferHandler.COPY_OR_MOVE

    override def createTransferable(c: JComponent) = new EntrySelection(c.asInstanceOf[JTable])

    override def exportDone(c: JComponent, t: Transferable, action: Int) {
    }

    override def canImport(ts: TransferHandler.TransferSupport) = {
      ts.setShowDropLocation(true)
      ts.getDataFlavors().find(EntrySelection.CUSTOM_FLAVOR == _).isDefined
    }

    override def importData(support: TransferHandler.TransferSupport): Boolean = {
      // if we can't handle the import, say so
      if (!canImport(support)) {
        return false;
      }

      // fetch the drop location
      val row = support.getDropLocation().asInstanceOf[JTable.DropLocation].getRow()

      // fetch the data and bail if this fails
      var data: EntrySelection = null
      try {
        data = support.getTransferable().getTransferData(EntrySelection.CUSTOM_FLAVOR).asInstanceOf[EntrySelection]
      } catch {
        case e => {
            e.printStackTrace();
            return false;
          }
      }

      // Apply action
      support.getDropAction() match  {
        case TransferHandler.MOVE => StarkMap.this.moveRows(row, data);
        case x => println("Undefined action: " +  x)
      }

      // Make change visible
      val table = support.getComponent().asInstanceOf[JTable]
      val rect = table.getCellRect(row, 0, false)
      if (rect != null) {
        table.scrollRectToVisible(rect)
      }

      true
    }
  }
}

object EntrySelection {
  val CUSTOM_FLAVOR = new DataFlavor(classOf[EntrySelection], "EntrySelection")
}

case class MapTableEntry(val visible: Boolean, val name: String)

class EntrySelection private(val entries: java.util.ArrayList[MapTableEntry]) extends Transferable with Serializable {
  def this(table: JTable) = {
    this(new java.util.ArrayList[MapTableEntry](table.getSelectedRows().length))
    for (row <- table.getSelectedRows()) {
      entries.add(
        new MapTableEntry(
          table.getValueAt(row, 0).asInstanceOf[java.lang.Boolean],
          table.getValueAt(row, 1).asInstanceOf[String]
        )
      )
    }
  }
  
  override def getTransferDataFlavors() = Array[DataFlavor](EntrySelection.CUSTOM_FLAVOR)

  override def isDataFlavorSupported(df: DataFlavor) = {
    getTransferDataFlavors().find(_ == df).isDefined
  }

  @throws(classOf[IOException])
  @throws(classOf[UnsupportedFlavorException])
  override def getTransferData(df: DataFlavor): AnyRef = {
    df match {
      case EntrySelection.CUSTOM_FLAVOR => this
      case _ => throw new IllegalArgumentException("Unsupported flavor " + df)
    }
  }
}
