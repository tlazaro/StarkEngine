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

object Node {
  val matrixes = new Pool[Matrix4](10) with SynchronizedPool[Matrix4] {
    override protected def newObject() = new Matrix4()
  }
  val vectors = new Pool[Vector3](10) with SynchronizedPool[Vector3] {
    override protected def newObject() = new Vector3()
  }
}

/** Node is the basic element to build from that can be displayed and updated on the screen */
trait Node extends Drawable with Updateable with Particle with Spatial {
  var parent: Option[Node] = None
  private var children = Vector[(String, Node)]()
  private var controller: Option[Controller[_ >: Node]] = None
  var touchEvent: TouchEvent.Value = TouchEvent.Empty
  var enabled = true

  var graphic: Graphic[_] = null
  var color: Color = Color.WHITE.cpy
  def width = if (graphic != null) graphic.width else -1
  def height = if (graphic != null) graphic.height else -1

  /** Returns the top most Node. It may return 'this' if it has no parent */
  def root: Node = {
    parent match {
      case Some(p) => p.root
      case _ => this
    }
  }

  /**
   * Does it make sense to talk about origin for Node without width and height?
   */
  def setOrigin(xRatio: Float, yRatio: Float) {
    originfX = xRatio
    originfY = yRatio
  }

  /**
   * Called when this node is touched or clicked
   */
  final def touched() {
    if (enabled)
      onTouch()
  }
  var onTouch: Unit => Unit = Unit => ()

  def kill() {
    for (p <- parent) {
      p.remove(this)
    }
  }

  protected def isOverChildren(pickX: Float, pickY: Float, strat: OverStrategy): Option[Node] = {
    for (child <- getChildren.reverse) {
      child.isOver(pickX, pickY, strat) match {
        case s @ Some(node) => return s
        case _ =>
      }
    }
    return None
  }

  def isOverLocal(pickX: Float, pickY: Float, strat: OverStrategy): Option[Node] = {
    if (width <= 0 || height <= 0) {
      isOverChildren(pickX, pickY, strat)
    } else {
      val res = if (graphic != null) {
        graphic.isOver(pickX - originX, pickY - originY, strat)
      } else {
        between(pickX, -originX, -originX + width) && between(pickY, -originY, -originY + height)
      }

      if (res) {
        isOverChildren(pickX, pickY, strat) match {
          case s @ Some(_) => s
          case _ => Some(this)
        }
      } else {
        None
      }
    }
  }

  def isOver(pickX: Float, pickY: Float, strat: OverStrategy = Contents): Option[Node] = {
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
    isOverLocal(tempx, tempy, strat)
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

  /** Draws this node and it's children */
  override def draw(spriteBatch: SpriteBatch) {
    if (graphic != null) {
      setDrawingColor(spriteBatch, color)
      graphic.draw(spriteBatch, x + xOffset - originX, y + yOffset - originY, originX + xOffset, originY + yOffset, width, height, scaleX, scaleY, rotation)
    }

    drawChildren(spriteBatch)
  }

  override def debugDraw(renderer: ShapeRenderer) {
    def drawRect(rect: Rectangle[Float]) {
      renderer.begin(ShapeType.Rectangle)
      renderer.rect(-originX + rect.x0, -originY + rect.y0, rect.width, rect.height)
      renderer.end()
    }

    def bounds() {
      if (graphic != null) {
        if (selected) {
          renderer.setColor(1f, 1f, 0f, 1f)
        } else {
          renderer.setColor(0f, 1f, 0f, 1f)
        }
        drawRect(graphic.bounds)
      }
    }

    def contents() {
      if (graphic != null) {
        renderer.setColor(1f, 0f, 0f, 1f)
        drawRect(graphic.contents)
      }
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
      bounds()
      contents()
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
    if (controller != null) {
      controller.foreach(ctrl => {
        ctrl.update(elapsed)
        if (ctrl.finished) {
          ctrl.onEnd()
          controller = None
        }
      })

    }
    updateChildren(elapsed)
  }

  def setController[T >: Node <: Updateable](controller: Controller[T]) {
    this.controller = Some(controller)
    controller.setTarget(this)
    controller.onStart()
  }

  def getController = controller
}
