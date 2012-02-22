package com.belfrygames.starkengine.core

import com.belfrygames.starkengine.tags._
import com.belfrygames.starkengine.utils._
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL10
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import scala.collection.mutable.ArrayBuffer

object Screen {
  var DEBUG = false
  var SHOW_KEYS = true
}

class Screen extends DrawableParent with UpdateableParent with Timed {
  import Screen._
  
  var app: StarkApp = _
  lazy val cam = new OrthographicCamera(app.config.width, app.config.height)
  lazy val followCam = new FollowCamera(cam)
  
  lazy val regularCam = new ScreenCam(cam)
  lazy val hudCam = new ScreenCam(new OrthographicCamera(app.config.width, app.config.height)) {
    cam.position.set(app.config.width / 2, app.config.height / 2, 0)
    cam.update()
  }
  
  lazy val specialCam = new FakeCam
  
  protected[this] val renderables = new ArrayBuffer[Drawable]
  protected[this] val specialRenderables = new ArrayBuffer[Drawable]
  
  protected[this] lazy val spriteBatch  = new SpriteBatch()
  private[this] lazy val font = new BitmapFont()
  
  private lazy val debugRenderer = new ShapeRenderer()
  
  def addSprite(sprite: Sprite) {
    regularCam.addDrawable(sprite)
    addUpdateable(sprite)
  }
  
  def removeSprite(sprite: Sprite) {
    regularCam.removeDrawable(sprite)
    removeUpdateable(sprite)
  }
  
  def register() {
  }
  
  def deregister() {
  }
  
  def create(app: StarkApp) {
    this.app = app
    
    addUpdateable(followCam)
    
    cam.position.set(0, 0, 0)
    followCam.update(tag(0))
    hudCam.cam.position.set(0, 0, 0)
    
    addDrawable(specialCam)
    addDrawable(regularCam)
    addDrawable(hudCam)
  }
  
  val tmp = new Vector3()
  val camDirection = new Vector3(1, 1, 0)
  val maxCamPosition = new Vector2(0, 0)
  
  var targetWidth = 0
  var targetHeight = 0
  
  def pick(x: Int, y: Int) = {
    tmp.x = x
    tmp.y = y
    tmp.z = 0
    cam.unproject(tmp)
    
    for(d <- regularCam.drawables) {
      d match {
        case s: Sprite => s.selected = s.isOver(tmp.x, tmp.y)
        case _ => d.selected = false
      }
    }
  }
  
  def draw() {
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
    
    Gdx.gl.glViewport((Gdx.graphics.getWidth - targetWidth) / 2, (Gdx.graphics.getHeight - targetHeight) / 2, targetWidth, targetHeight)
    
    draw(spriteBatch)
    if (Screen.DEBUG) {
      debugRenderer.setProjectionMatrix(cam.combined.cpy);
      debugDraw(debugRenderer)
    }
    
    Gdx.gl.glViewport(0,0, Gdx.graphics.getWidth, Gdx.graphics.getHeight)
    
    if (SHOW_KEYS || DEBUG) {
      spriteBatch.begin()
      if (SHOW_KEYS) {
        for ((text, line) <- keys) {
          font.draw(spriteBatch, text, 20, app.config.height - ((line + 1) * 20))
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
- Arrows or WASD: Move
- X or Space: Action
- +/- Zoom in/out
- Esc : Exit""".split("\n").view.zipWithIndex.toList
  
  def resume( ) { }
  def resize(width : Int, height : Int) {
    targetWidth = width
    targetHeight = height
  }
  
  def pause( ) { }
  def dispose( ) { }
}
