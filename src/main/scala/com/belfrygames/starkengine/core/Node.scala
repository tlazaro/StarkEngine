package com.belfrygames.starkengine.core

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import com.belfrygames.starkengine.tags._
import com.belfrygames.starkengine.utils._
import com.starkengine.utils.SynchronizedPool
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL10

object Node {
  val matrixes = new Pool[Matrix4](10) with SynchronizedPool[Matrix4] {
    override protected def newObject() = new Matrix4()
  }
  val vectors = new Pool[Vector3](10) with SynchronizedPool[Vector3] {
    override protected def newObject() = new Vector3()
  }
  val rectangles = new Pool[com.badlogic.gdx.math.Rectangle](10) with SynchronizedPool[com.badlogic.gdx.math.Rectangle] {
    override protected def newObject() = new com.badlogic.gdx.math.Rectangle()
  }

  val CENTER = Point2D(0.5f, 0.5f)
  val NORTH = Point2D(0.5f, 1f)
  val NORTH_EAST = Point2D(1f, 1f)
  val EAST = Point2D(1f, 0.5f)
  val SOUTH_EAST = Point2D(1f, 0f)
  val SOUTH = Point2D(0.5f, 0f)
  val SOUTH_WEST = Point2D(0f, 0f)
  val WEST = Point2D(0f, 0.5f)
  val NORTH_WEST = Point2D(0, 1f)

  sealed trait Touch
  case object Consume extends Touch
  case object Passthrough extends Touch
  case object Ignore extends Touch
}

/** Node is the basic element to build from that can be displayed and updated on the screen */
trait Node extends Drawable with Updateable with Particle with Spatial {
  var parent: Option[Node] = None
  private var children = Vector[(String, Node)]()
  private var controller: Option[Controller[_ >: Node]] = None
  var touchEvent: TouchEvent.Value = TouchEvent.Empty
  var enabled = true
  var clipRect: Rectangle[Float] = null

  var graphic: Graphic[_] = null
  private var _color: Color = Color.WHITE.cpy
  def color: Color = _color
  def color_=(color: Color) { _color = color }

  var respondOver = true

  def width = if (graphic != null) graphic.width else -1
  def height = if (graphic != null) graphic.height else -1

  /** Returns the top most Node. It will return 'this' if it has no parent */
  def root: Node = parent.map(_.root).getOrElse(this)

  /**
   * Sets origin ratio for this Node.
   * Does it make sense to talk about origin for Node without width and height?
   */
  def setOrigin(xRatio: Float, yRatio: Float) {
    originfX = xRatio
    originfY = yRatio
  }

  /**
   * Sets origin ratio for this Node.
   */
  def setOrigin(ratio: Point2D[Float]) {
    setOrigin(ratio.x, ratio.y)
  }

  def bounds: Rectangle[Float] = if (graphic == null) Rectangle.EMPTY_FLOAT else graphic.bounds.move(Point2D(-originX, -originY))
  def contents: Rectangle[Float] = if (graphic == null) Rectangle.EMPTY_FLOAT else graphic.contents.move(Point2D(-originX, -originY))

  def clearClipping() {
    clipRect = null
  }

  /**
   * Called when this node is touched or clicked
   */
  final def touched() = {
    enabled && onTouch() == Node.Consume
  }
  var onTouch: () => Node.Touch = () => Node.Ignore

  def kill() {
    for (p <- parent) {
      p.remove(this)
    }
  }

  @inline private def loadClipBounds(clipBounds: com.badlogic.gdx.math.Rectangle) {
    clipBounds.set(clipRect.x0 + x + xOffset - originX, clipRect.y0 + y + yOffset - originY, clipRect.width, clipRect.height)
  }

  private def isInsideVisibleArea(pickX: Float, pickY: Float) = {
    if (!visible) false
    else if (clipRect == null) true
    else {
      val clipBounds = Node.rectangles.obtain()
      loadClipBounds(clipBounds)
      val res = clipBounds.contains(pickX, pickY)
      Node.rectangles.free(clipBounds)
      res
    }
  }

  private def isInsideVisibleAreaLocal(pickX: Float, pickY: Float) = {
    if (!visible) false
    else if (clipRect == null) true
    else clipRect.contains(pickX, pickY)
  }

  protected def isOverChildren(pickX: Float, pickY: Float, strat: OverStrategy, behavior: OverBehavior): Option[Node] = {
    if (!isInsideVisibleAreaLocal(pickX, pickY))
      return None

    for (child <- getChildren.reverse) {
      child.isOver(pickX, pickY, strat, behavior) match {
        case s @ Some(_) => return s
        case _ =>
      }
    }
    None
  }

  def isOverLocal(pickX: Float, pickY: Float, strat: OverStrategy, behavior: OverBehavior): Option[Node] = {
    if (!isInsideVisibleAreaLocal(pickX, pickY))
      return None

    if (width <= 0 || height <= 0) {
      isOverChildren(pickX, pickY, strat, behavior)
    } else {
      val res = graphic != null && graphic.isOver(pickX + originX, pickY + originY, strat)

      isOverChildren(pickX, pickY, strat, behavior) match {
        case s @ Some(_) => s
        case _ if res => Some(this)
        case _ => None
      }
    }
  }

  def isOver(pickX: Float, pickY: Float, strat: OverStrategy = Contents, behavior: OverBehavior = All): Option[Node] = {
    if (!visible)
      return None

    def isOver0 = {
      val m = Node.matrixes.obtain().idt()
      m.scale(1 / scaleX, 1 / scaleY, 1f)
      m.rotate(0, 0, 1f, -rotation)
      m.translate(-(x + xOffset), -(y + yOffset), 0f)

      val tmp = Node.vectors.obtain()
      tmp.x = pickX
      tmp.y = pickY
      tmp.z = 0
      tmp.mul(m)

      Node.matrixes.free(m)

      val tempx = tmp.x
      val tempy = tmp.y
      Node.vectors.free(tmp)
      isOverLocal(tempx, tempy, strat, behavior)
    }

    behavior match {
      case All => isOver0
      case OnlyEnabled => if (respondOver) isOver0 else None
    }
  }

  /** Adds a Node to be rendered and updated */
  final def add[T <: Node](child: T, name: String): T = synchronized {
    children = children :+ ((name, child))
    child.parent = Some(this)
    child
  }

  /** Removes a Node by name */
  final def remove(name: String): Option[Node] = synchronized {
    children.indexWhere(_._1 == name) match {
      case n if n >= 0 => {
        val child = children(n)._2
        child.parent = None
        val (prefix, suffix) = children splitAt n
        children = prefix ++ suffix.tail
        Some(child)
      }
      case _ => None
    }
  }

  /** Removes a Node by name */
  final def remove(child: Node): Option[Node] = synchronized {
    children.toIterable.find(_._2 == child) match {
      case Some(pair) => remove(pair._1)
      case _ => None
    }
  }

  final def removeAll() = synchronized {
    for ((id, child) <- children) {
      child.parent = None
    }
    children = Vector()
  }

  /** Find a child by name */
  final def get(child: String): Option[Node] = children.find(_._1 == child).map(_._2)

  final def getChildren() = children.map(_._2)

  private var _matrix: Matrix4 = null
  final def matrix(): Matrix4 = {
    if (_matrix == null) {
      _matrix = Node.matrixes.obtain()
    }

    _matrix.idt
    _matrix.translate(x, y, 0f)
    _matrix.rotate(0f, 0f, 1f, rotation)
    _matrix.scale(scaleX, scaleY, 1f)

    _matrix
  }

  override def finalize() {
    if (_matrix != null) {
      Node.matrixes.free(_matrix)
    }
    super.finalize()
  }

  final protected def drawChildren(spriteBatch: SpriteBatch) = {
    if (!children.isEmpty) {
      spriteBatch.end()
      spriteBatch.begin()
      val trans = spriteBatch.getTransformMatrix
      val oldTrans = Node.matrixes.obtain().set(trans)
      trans.mul(matrix)
      spriteBatch.setTransformMatrix(trans)

      for (child <- children) {
        setDrawingColor(spriteBatch, color)
        child._2 redraw spriteBatch
      }

      spriteBatch.setTransformMatrix(oldTrans)
      Node.matrixes.free(oldTrans)
    }
  }

  final protected def debugDrawChildren(renderer: ShapeRenderer) = {
    children foreach (_._2 debugRedraw renderer)
  }

  protected def setDrawingColor(spriteBatch: SpriteBatch, color: Color) {
    spriteBatch.setColor(color)
  }

  private def findLayer: Option[Layer] = {
    if (this.isInstanceOf[Layer]) {
      Some(this.asInstanceOf[Layer])
    } else {
      parent.flatMap(_.findLayer)
    }
  }

  /** Draws this node and it's children */
  override def draw(spriteBatch: SpriteBatch) {
    var scissors: com.badlogic.gdx.math.Rectangle = null
    val clipped = if (clipRect != null) {
      findLayer match {
        case Some(layer) =>
          spriteBatch.flush()
          scissors = Node.rectangles.obtain()
          val clipBounds = Node.rectangles.obtain()
          loadClipBounds(clipBounds)
          ScissorStack.calculateScissors(layer.cam, spriteBatch.getTransformMatrix(), clipBounds, scissors)
          Node.rectangles.free(clipBounds)
          val appliedScissors = ScissorStack.pushScissors(scissors)
          if (!appliedScissors)
            Node.rectangles.free(scissors)
          appliedScissors
        case _ => false
      }
    } else false

    if (graphic != null) {
      setDrawingColor(spriteBatch, color)
      graphic.draw(spriteBatch, x + xOffset - originX, y + yOffset - originY, originX + xOffset, originY + yOffset, width, height, scaleX, scaleY, rotation)

    }

    drawChildren(spriteBatch)

    if (clipped) {
      spriteBatch.flush()
      ScissorStack.popScissors()
      Node.rectangles.free(scissors)
    }
  }

  protected def bounds(renderer: ShapeRenderer) {
    if (graphic != null) {
      if (selected) {
        renderer.setColor(1f, 1f, 0f, 1f)
      } else {
        renderer.setColor(0f, 1f, 0f, 1f)
      }
      drawRect(renderer, graphic.bounds)
    }
  }

  protected def contents(renderer: ShapeRenderer) {
    if (graphic != null) {
      renderer.setColor(1f, 0f, 0f, 1f)
      drawRect(renderer, graphic.contents)
    }
  }

  protected def clip(renderer: ShapeRenderer) {
    if (clipRect != null) {
      renderer.setColor(0f, 0f, 1f, 1f)
      drawRect(renderer, clipRect)
    }
  }

  protected def drawRect(renderer: ShapeRenderer, rect: Rectangle[Float]) {
    renderer.begin(ShapeType.Rectangle)
    renderer.rect(-originX + rect.x0, -originY + rect.y0, rect.width, rect.height)
    renderer.end()
  }

  override def debugDraw(renderer: ShapeRenderer) {
    def drawRect(rect: Rectangle[Float]) {
      renderer.begin(ShapeType.Rectangle)
      renderer.rect(-originX + rect.x0, -originY + rect.y0, rect.width, rect.height)
      renderer.end()
    }

    def cross() {
      renderer.setColor(1f, 1f, 1f, 1f)
      renderer.begin(ShapeType.Line)
      renderer.line(-5, -5, 5, 5)
      renderer.line(-5, 5, 5, -5)
      renderer.end()
    }

    def pointsInside() {
      renderer.begin(ShapeType.Point)
      for (x <- Range.Double.inclusive(-512, 512, 5); y <- Range.Double.inclusive(-320, 320, 5)) {
        if (isOver(x.toFloat, y.toFloat).isDefined) {
          renderer.setColor(1f, 0f, 0f, 0.5f)
          renderer.point(0, 0, 0)
        }
      }
      renderer.end()
    }

    val transX = x + xOffset
    val transY = y + yOffset

    renderer.translate(transX, transY, 0f)
    renderer.rotate(0f, 0f, 1f, rotation)
    renderer.scale(scaleX, scaleY, 1f)

    if (width > 0 && height > 0) {
      bounds(renderer)
      contents(renderer)
      clip(renderer)
      cross()
    }

    debugDrawChildren(renderer)

    renderer.scale(1.0f / scaleX, 1.0f / scaleY, 1f)
    renderer.rotate(0f, 0f, 1f, -rotation)
    renderer.translate(-transX, -transY, 0f)
  }

  final protected def updateChildren(elapsed: Long @@ Milliseconds) { children foreach (_._2 update elapsed) }

  /** Updates this node and it's children */
  override def update(elapsed: Long @@ Milliseconds) {
    for (ctrl <- controller) {
      ctrl.update(elapsed)
      if (ctrl.finished) {
        ctrl.onEnd()
        controller = None
      }
    }

    updateChildren(elapsed)
  }

  def setController[T >: Node <: Updateable](controller: Controller[T]) {
    this.controller = Some(controller)
    controller.setTarget(this)
    controller.onStart()
  }

  def getController = controller

  /** Removes the current controller from the Node. Doesn't give a chance to the controller to finish. Use with care. */
  def clearController() {
    controller = None
  }
}
