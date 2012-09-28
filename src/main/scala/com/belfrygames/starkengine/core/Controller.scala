package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._
import scala.collection.mutable.ListBuffer
import com.badlogic.gdx.graphics.Color

abstract class Controller[T <: Updateable] extends Updateable {
  var target: T = _

  final def setTarget(t: T) {
    target = t
  }

  def finished: Boolean

  /** Called when in need of forcing the end of the controller */
  def forceFinish()

  /** Called by controllee when started using controller */
  def onStart() {}

  /** Called by controllee when finished using controller */
  def onEnd() {}
}

trait Overtime[T <: Updateable] extends Controller[T] {
  var overtime = tag[Milliseconds](0L)

  abstract override def onStart() {
    super.onStart()
    overtime = tag[Milliseconds](0L)
  }
}

trait Instant[T <: Updateable] extends Controller[T] {
}

abstract class TimedController[T <: Updateable](var duration: Long @@ Milliseconds) extends Controller[T] with Overtime[T] {
  var interpolation: Function1[Float, Float] = x => x

  var time = tag[Milliseconds](0L)

  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)

    time = tag[Milliseconds](time + elapsed)
    if (time > duration) {
      overtime = tag[Milliseconds](time - duration)
      time = duration
    }
  }

  /** Gets the elapsed fraction for this controller. Returns a value in the range [0.0..1.0].*/
  def fraction(): Float = time.toFloat / duration.toFloat

  /**
   * Gets the elapsed fraction after applying the interpolated function.
   * By default uses the identity function over the linearly increasing fraction.
   */
  def interpolate(): Float = interpolation(fraction)

  override def onStart() {
    super.onStart()
    time = tag[Milliseconds](0L)
  }

  def finished = time >= duration

  def forceFinish() = time = duration
}

trait Smoothstep { self: TimedController[_] =>
  self.interpolation = x => { (x * x * (3 - 2 * x)) }
}

trait Bias { self: TimedController[_] =>
  var bias: Float
  self.interpolation = x => {
    scala.math.pow(x, scala.math.log(bias) / scala.math.log(0.5f)).toFloat
  }
}

trait Gain { self: TimedController[_] =>
  var gain: Float
  private def bias(b: Float, x: Float) = scala.math.pow(x, scala.math.log(b) / scala.math.log(0.5f)).toFloat
  self.interpolation = x => {
    if (x < 0.5f) {
      bias(1 - gain, 2 * x) / 2f
    } else {
      1 - bias(1 - gain, 2 - 2 * x) / 2f
    }
  }
}

class Delay[T <: Updateable](duration: Long @@ Milliseconds) extends TimedController[T](duration)

class NodeController(duration: Long @@ Milliseconds) extends TimedController[Node](duration) {
}

class Tint(val dest: Color, duration0: Long @@ Milliseconds) extends TimedController[Node](duration0) {
  var start: Color = new Color
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    target.color.a = start.a + (dest.a - start.a) * interpolate
    target.color.r = start.r + (dest.r - start.r) * interpolate
    target.color.g = start.g + (dest.g - start.g) * interpolate
    target.color.b = start.b + (dest.b - start.b) * interpolate
  }

  /** Called by controllee when started using controller */
  override def onStart() {
    super.onStart()
    start.set(target.color)
  }

  /** Called by controllee when finished using controller */
  override def onEnd() {
    super.onEnd()
    target.color.set(dest)
  }
}

/**
 * Given a Point2D moves the Node to that place regardless of starting position
 */
class MoveTo(val dest: Point2D[Float], duration0: Long @@ Milliseconds) extends TimedController[Node](duration0) {
  var start = Point2D(0.0f, 0.0f)
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)

    target.x = start.x + (dest.x - start.x) * interpolate
    target.y = start.y + (dest.y - start.y) * interpolate
  }

  /** Called by controllee when started using controller */
  override def onStart() {
    super.onStart()

    start = Point2D(target.x, target.y)
    val distance = math.sqrt((start.x - dest.x) * (start.x - dest.x) + (start.y - dest.y) * (start.y - dest.y))
    // calculate duration based on speed if needed
  }

  /** Called by controllee when finished using controller */
  override def onEnd() {
    super.onEnd()

    target.x = dest.x
    target.y = dest.y
  }
}

/**
 * Given a vector as a Point2D moves the Node across that distance from it's starting positon.
 */
class Move(val vector: Point2D[Float], duration0: Long @@ Milliseconds) extends TimedController[Node](duration0) {
  var start = Point2D(0.0f, 0.0f)
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)

    target.x = start.x + vector.x * interpolate
    target.y = start.y + vector.y * interpolate
  }

  /** Called by controllee when started using controller */
  override def onStart() {
    super.onStart()

    start = Point2D(target.x, target.y)
  }

  /** Called by controllee when finished using controller */
  override def onEnd() {
    super.onEnd()

    target.x = start.x + vector.x
    target.y = start.y + vector.y
  }
}

class Rotate(val amount: Float, duration0: Long @@ Milliseconds) extends TimedController[Node](duration0) {
  var start = 0f
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    target.rotation = start + amount * interpolate
  }

  /** Called by controllee when started using controller */
  override def onStart() {
    super.onStart()
    start = target.rotation
  }

  /** Called by controllee when finished using controller */
  override def onEnd() {
    super.onEnd()
    target.rotation = start + amount
  }
}

class Scale(val scaleX: Float, val scaleY: Float, duration0: Long @@ Milliseconds) extends TimedController[Node](duration0) {
  var startX = 0f
  var startY = 0f
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    target.scaleX = startX + startX * (scaleX - 1) * interpolate
    target.scaleY = startY + startY * (scaleY - 1) * interpolate
  }

  /** Called by controllee when started using controller */
  override def onStart() {
    super.onStart()
    startX = target.scaleX
    startY = target.scaleY
  }

  /** Called by controllee when finished using controller */
  override def onEnd() {
    super.onEnd()
    target.scaleX = startX * scaleX
    target.scaleY = startY * scaleY
  }
}

class AbsoluteScale(val scaleStart: (Float, Float), val scaleEnd: (Float, Float), duration0: Long @@ Milliseconds) extends TimedController[Node](duration0) {
  var startX = 0f
  var startY = 0f
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    target.scaleX = startX + (scaleEnd._1 - startX) * interpolate
    target.scaleY = startY + (scaleEnd._2 - startY) * interpolate
  }

  /** Called by controllee when started using controller */
  override def onStart() {
    super.onStart()
    startX = scaleStart._1
    startY = scaleStart._2
  }

  /** Called by controllee when finished using controller */
  override def onEnd() {
    super.onEnd()
    target.scaleX = scaleEnd._1
    target.scaleY = scaleEnd._2
  }
}

class ControllerQueue[T <: Updateable](controllers0: Controller[T]*) extends Controller[T] {
  protected val controllers = new ListBuffer[Controller[T]]()

  def add(controller: Controller[T]) {
    controllers += controller
  }

  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    if (!controllers.isEmpty) {
      controllers.head.update(elapsed)
      if (controllers.head.finished) {
        controllers.head.onEnd()
        controllers.remove(0)
        if (!controllers.isEmpty) {
          controllers.head.onStart()
        }
      }
    }
  }

  /** Called by controllee when started using controller */
  override def onStart() {
    super.onStart()
    controllers0 foreach add
    controllers foreach (_.target = target)

    if (!controllers.isEmpty) {
      controllers.head.onStart()
    }
  }

  def finished = controllers.isEmpty

  def forceFinish() {
    controllers.foreach(_.forceFinish())
  }
}

class ControllerSet[T <: Updateable](controllers0: Controller[T]*) extends Controller[T] {
  protected var controllers = new ListBuffer[Controller[T]]()

  def add(controller: Controller[T]) {
    controllers += controller
  }

  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)

    if (!controllers.isEmpty) {
      controllers.foreach(_.update(elapsed))
      val (finished, notFinished) = controllers.partition(_.finished)
      finished.foreach(_.onEnd())
      controllers = notFinished
    }
  }

  override def onStart() {
    super.onStart()

    controllers0 foreach add

    controllers foreach (_.target = target)

    controllers.foreach(_.onStart())
  }

  def finished = controllers.isEmpty

  def forceFinish() {
    controllers.foreach(_.forceFinish())
  }
}

/**
 * The ControllerLoop keeps executing the same controller over and over again. In order for that to work
 * controllers must be reset with the onStart() method. The only way for this controller to end is using forceFinish().
 */
class ControllerLoop[T <: Updateable](protected val controller: Controller[T]) extends Controller[T] {
  private var forcedFinish = false

  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)

    controller.update(elapsed)

    if (controller.finished) {
      controller match {
        case c: Overtime[_] => {
          do {
            c.onEnd()

            val over = c.overtime
            c.onStart()
            if (over > 0) {
              c.update(over)
            }
          } while (c.finished)
        }
        case _ => {
          controller.onEnd()
          controller.onStart()
        }
      }
    }
  }

  /** Called by controllee when started using controller */
  override def onStart() {
    super.onStart()
    controller.target = target
    controller.onStart()
  }

  def finished = forcedFinish

  def forceFinish() {
    controller.forceFinish()
    forcedFinish = true
  }
}

class ControllerAction[T <: Updateable](private val f: T => Unit) extends Controller[T] with Instant[T] {
  private var _finished = false
  def forceFinish() { _finished = true }
  def finished: Boolean = _finished
  override def update(elapsed: Long @@ Milliseconds) {
    _finished = true
  }
  override def onEnd() {
    f(target)
  }
}

object Controller {
  //  def apply[T <: Updateable](f: T => Unit): Controller[T] = {
  //    new Controller[T]() {
  //      def update(elapsed: Long @@ Milliseconds) {
  //        f(target)
  //      }
  //    }
  //  }
}
