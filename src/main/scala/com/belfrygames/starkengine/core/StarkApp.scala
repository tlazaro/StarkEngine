package com.belfrygames.starkengine.core

import com.badlogic.gdx.ApplicationListener
import com.belfrygames.starkengine.utils.StopWatch
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.belfrygames.starkengine.tags._

sealed trait ResizePolicy
case object FitScreen extends ResizePolicy
case object Stretch extends ResizePolicy
case object Original extends ResizePolicy

class StarkApp(val config: Config) extends ApplicationListener with Updateable with Timed {
  var resizePolicy: ResizePolicy = FitScreen
  private[this] var targetWidth = 0
  private[this] var targetHeight = 0
  private[this] var _screen: Screen = _
  
  lazy val inputs = new InputMultiplexer()
  lazy val res = config.resources
  
  def screen = _screen
  def screen_=(value: Screen) = {
    _screen = value
    _screen.create(this)
  }
  
  protected[this] val timer = new StopWatch
  
  def create() {
    Gdx.app.log("StarkApp", "Created Stark App")
    
    Gdx.input.setInputProcessor(inputs)
    inputs.addProcessor(new DebugKeysController(this))
    
    config.resources.initialize()
    screen = config.firstScreen
    
    timer.start
  }
  
  final def render() {
    timer.end
    screen.draw()
    nanoUpdate(timer.diff)
    timer.start
  }
  
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    screen.update(elapsed)
  }
  
  def resume() {
    screen.resume()
  }
  
  def resize(width : Int, height : Int) {
    val (tWidth, tHeight) = resizePolicy match {
      case FitScreen => if (width.toFloat / height >= config.width.toFloat / config.height) {
          (height * config.width / config.height, height)
        } else {
          (width, width * config.height / config.width)
        }
      case Stretch => (width, height)
      case Original => (config.width, config.height)
    }
    targetWidth = tWidth
    targetHeight = tHeight
    
    screen.resize(targetWidth, targetHeight)
  }
  
  def pause() {
    Gdx.app.log("StarkApp", "Paused Stark App")
    
    screen.pause()
  }
  
  def dispose() {
    Gdx.app.log("StarkApp", "Disposed Stark App")
    
    screen.dispose()
  }
}

class DebugKeysController(app: StarkApp) extends InputAdapter {
  import com.badlogic.gdx.Input.Keys._

  override def keyUp(keycode : Int) : Boolean = {
    keycode match {
      case ESCAPE => Gdx.app.exit()
      case F2 => app.resizePolicy = FitScreen; app.resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
      case F3 => app.resizePolicy = Original; app.resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
      case F4 => app.resizePolicy = Stretch; app.resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
      case _ =>
    }

    false
  }
  
  var oldX = 0
  var oldY = 0
  
  override def touchMoved(x: Int, y: Int) = {
    app.screen.pick(x, y)
    false
  }
  
  override def touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean = {
    oldX = x
    oldY = y
    false
  }
  
  override def touchDragged(x: Int, y: Int, pointer: Int): Boolean = {
    val pos = app.screen.regularCam.cam.position
    pos.x -= x - oldX
    pos.y += y - oldY
    oldX = x
    oldY = y
    false
  }
}