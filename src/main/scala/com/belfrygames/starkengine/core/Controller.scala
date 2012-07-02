package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._
import scala.collection.mutable.ListBuffer

abstract class Controller[T <: Updateable] extends Updateable {
  private[core] var target: T = _
  
  def setTarget(t: T) {
    target = t
  }
  
  def finished(): Boolean

  /** Called when in need of forcing the end of the controller */
  def forceFinish()
  
  /** Called by controllee when started using controller */
  def onStart() {}
  
  /** Called by controllee when finished using controller */
  def onEnd() {}
}

abstract class TimedController[T <: Updateable](var duration: Long @@ Milliseconds) extends Controller[T] {
  var time = tag[Milliseconds](0L)
  var overtime = tag[Milliseconds](0L)
  
  override def update(elapsed: Long @@ Milliseconds) {
    time = tag[Milliseconds](time + elapsed)
    if (time > duration) {
      overtime = tag[Milliseconds](time - duration)
      time = duration
    }
  }
  
  /** Gets the elapsed fraction for this controller. Returns a value in the range [0.0..1.0].*/
  def fraction(): Float = time.toFloat / duration.toFloat
  
  def finished() = time >= duration
  
  def forceFinish() = time = duration
}

class NodeController(duration: Long @@ Milliseconds) extends TimedController[Node](duration) {
}

class MoveTo(val dest: Point2D[Float], duration0: Long @@ Milliseconds) extends TimedController[Node](duration0) {
  var start = Point2D(0.0f, 0.0f)
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    
    target.x = start.x + (dest.x - start.x) * fraction
    target.y = start.y + (dest.y - start.y) * fraction
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

class Rotate(val amount: Float, duration0: Long @@ Milliseconds) extends TimedController[Node](duration0) {
  var last = 0f
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    
    last = (amount - last) * fraction
    target.rotation += last
  }
  
  /** Called by controllee when started using controller */
  override def onStart() {
    super.onStart()
  }
  
  /** Called by controllee when finished using controller */
  override def onEnd() {
    super.onEnd()
  }
}


class ControllerQueue[T <: Updateable](controllers0: Controller[T]*) extends Controller[T] {
  protected val controllers = new ListBuffer[Controller[T]]()
  controllers0 foreach add
  
  def add(controller: Controller[T]) {
    controllers += controller
  }
  
  override def update(elapsed: Long @@ Milliseconds) {
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
    controllers foreach (_.target = target)
    
    if (!controllers.isEmpty) {
      controllers.head.onStart()
    }
  }
  
  def finished() = controllers.isEmpty
  
  def forceFinish() {
    controllers.foreach(_.forceFinish)
    controllers.clear
  }
}

class ControllerSet[T <: Updateable](controllers0: Controller[T]*) extends Controller[T] {
  protected var controllers = new ListBuffer[Controller[T]]()
  controllers0 foreach add
  
  def add(controller: Controller[T]) {
    controllers += controller
  }
  
  override def update(elapsed: Long @@ Milliseconds) {
    if (!controllers.isEmpty) {
      controllers.foreach(_.update(elapsed))
      val (finished, notFinished) = controllers.partition(_.finished)
      finished.foreach(_.onEnd)
      controllers = notFinished
    }
  }
  
  override def onStart() {
    super.onStart()
    controllers foreach (_.target = target)
    
    controllers.foreach(_.onStart)
  }
  
  def finished() = controllers.isEmpty
  
  def forceFinish() {
    controllers.foreach(_.forceFinish)
    controllers.clear
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
