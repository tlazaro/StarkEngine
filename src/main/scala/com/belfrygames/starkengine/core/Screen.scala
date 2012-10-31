package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._
import com.belfrygames.starkengine.utils._
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.GL10
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import scala.collection.mutable.ArrayBuffer
import com.badlogic.gdx.graphics.Camera

object Screen {
  var DEBUG = false
  var SHOW_KEYS = false
}

class Screen extends Node with Timed {
  import Screen._

  var app: StarkApp = _
  // TODO make private, access only through Layer
  lazy val cam = new OrthographicCamera(app.config.width, app.config.height)
  lazy val followCam = new FollowCamera(cam)

  // TODO make private, access only through Layer
  lazy val hudCam = new OrthographicCamera(app.config.width, app.config.height) {
    position.set(app.config.width / 2, app.config.height / 2, 0)
    update()
  }

  protected var created = false
  def isCreated() = created

  protected[this] val renderables = new ArrayBuffer[Drawable]
  protected[this] val specialRenderables = new ArrayBuffer[Drawable]

  protected[this] lazy val spriteBatch = new SpriteBatch()
  private[this] lazy val font = new BitmapFont()

  private lazy val debugRenderer = new ShapeRenderer()

  lazy val foreground = new Layer(cam)
  lazy val hud: Layer = new Layer(hudCam)

  /** Called by StarkApp when it sets this as current screen */
  def register() {
  }

  /** Called by StarkApp when this is the current screen and is being replaced */
  def deregister() {
  }

  def create(app: StarkApp) {
    this.app = app

    add(foreground, "foreground")
    add(hud, "hud")

    cam.position.set(0, 0, 0)
    followCam.update(tag(0))
    hudCam.position.set(0, 0, 0)

    app.inputs.addProcessor(new ScreenDefeaultInputController())
    app.inputs.addProcessor(new ScreenDebugKeysController())

    created = true
  }

  val tmp = new Vector3()
  val camDirection = new Vector3(1, 1, 0)
  val maxCamPosition = new Vector2(0, 0)

  var targetWidth = 0
  var targetHeight = 0

  def screenToViewPortX(x: Float) = {
    (x - (Gdx.graphics.getWidth - targetWidth) / 2) * (Gdx.graphics.getWidth.toFloat / targetWidth.toFloat)
  }

  def screenToViewPortY(y: Float) = {
    (y - (Gdx.graphics.getHeight - targetHeight) / 2) * (Gdx.graphics.getHeight.toFloat / targetHeight.toFloat)
  }

  def screenToCanvas(x: Int, y: Int, result: Vector3 = null, camera: Camera = null): Vector3 = {
    val vec = if (result == null) new Vector3 else result
    vec.x = screenToViewPortX(x)
    vec.y = screenToViewPortY(y)
    vec.z = 0
    if (camera == null) {
      vec.y = Gdx.graphics.getHeight - vec.y // Fix inverted Y-Axis
      vec 
    } else {
     camera.unproject(vec)
     vec
    }
  }

  def pick(x: Int, y: Int) {
    def select(selected: Node) {
      if (!selected.selected) {
        selected.selected = true
        selected.touchEvent = TouchEvent.Entered
      }

      if (TouchEvent.over.isDefined) {
        if (TouchEvent.over.get != selected) {
          TouchEvent.over.get.selected = false
          TouchEvent.over.get.touchEvent = TouchEvent.Exited
          TouchEvent.over = Some(selected)
        }
      } else {
        TouchEvent.over = Some(selected)
      }
    }

    def clearSelection() {
      TouchEvent.over foreach { n =>
        n.selected = false
        n.touchEvent = TouchEvent.Exited
      }
      TouchEvent.over = None
    }

    // Check hud first
    screenToCanvas(x, y, tmp, hudCam)
    hud.isOver(tmp.x, tmp.y, Contents, OnlyEnabled) match {
      case Some(selected) if selected != hud =>
        select(selected)
      case _ =>
        // Foreground converts coords
        screenToCanvas(x, y, tmp, cam)
        foreground.isOver(tmp.x, tmp.y, Contents, OnlyEnabled) match {
          case Some(selected) if selected != foreground => select(selected)
          case _ => clearSelection
        }
    }
  }

  def draw() {
    Gdx.gl.glViewport((Gdx.graphics.getWidth - targetWidth) / 2, (Gdx.graphics.getHeight - targetHeight) / 2, targetWidth, targetHeight)

    spriteBatch.begin()
    draw(spriteBatch)
    spriteBatch.end()

    if (Screen.DEBUG) {
      debugDraw(debugRenderer)
    }

    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth, Gdx.graphics.getHeight)

    if (SHOW_KEYS || DEBUG) {
      spriteBatch.begin()
      if (SHOW_KEYS) {
        for ((text, line) <- keys) {
          font.draw(spriteBatch, text, 20, Gdx.graphics.getHeight - ((line + 2) * 20))
        }
      }
      if (DEBUG) {
        font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20)
        font.draw(spriteBatch, "Resize Policy: " + app.resizePolicy.getClass.getSimpleName, 20, 40)
        tmp.set(0, 0, 0)
        cam.unproject(tmp)
        font.draw(spriteBatch, "Location: " + cam.position.x + "," + cam.position.y, 20, 60)
        font.draw(spriteBatch, "Mouse: " + Gdx.input.getX() + "," + Gdx.input.getY(), 20, 80)
      }
      spriteBatch.end()
    }
  }

  private[this] val keys = """Keys:
- Tab : Toggle Debug info
- F1 : Toggle Keys help
- F2 : Screen: FitScreen
- F3 : Screen: Original
- F4 : Screen: Stretch
- F5 : Screen: Original Canvas
- Arrows or WASD: Move
- X or Space: Action
- +/- Zoom in/out
- Esc : Exit""".split("\n").view.zipWithIndex.toList

  def resume() {}
  def resize(width: Int, height: Int) {
    targetWidth = width
    targetHeight = height

    app.resizePolicy match {
      case OriginalCanvas => {
        cam.viewportWidth = targetWidth
        cam.viewportHeight = targetHeight
        hudCam.viewportWidth = targetWidth
        hudCam.viewportHeight = targetHeight
      }
      case _ => {
        cam.viewportWidth = app.config.width
        cam.viewportHeight = app.config.height
        hudCam.viewportWidth = app.config.width
        hudCam.viewportHeight = app.config.height
      }
    }
  }

  def pause() {}
  def dispose() {}
}

class ScreenDebugKeysController extends InputAdapter {
  import com.badlogic.gdx.Input.Keys._

  override def keyUp(keycode: Int): Boolean = {
    keycode match {
//      case TAB => Screen.DEBUG = !Screen.DEBUG
//      case F1 => Screen.SHOW_KEYS = !Screen.SHOW_KEYS
      case _ =>
    }

    false
  }
}

class ScreenDefeaultInputController extends InputAdapter {
  import com.badlogic.gdx.Input.Keys._

  override def touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean = {
    TouchEvent.over.map(_.touched()).getOrElse(false)
  }
}