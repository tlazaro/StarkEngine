package com.belfrygames.starkengine.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.math.Vector3

class ScreenTest extends Screen {
  override def register() {
    super.register()
  }
  
  override def deregister() {
    super.deregister()
  }
  
  lazy val ed = Sprite(app.res.get("eddard"))
  lazy val ed2 = Sprite(app.res.get("eddard"))
  lazy val ed3 = Sprite(app.res.get("eddard"))
  lazy val ed4 = Sprite(app.res.get("eddard"))
  lazy val cursor = Sprite(app.res.get("cursor"))
  
  override def create(app: StarkApp) {
    super.create(app)
    
    Screen.DEBUG = true
    
    addSprite(ed)
    addSprite(ed2)
    addSprite(ed3)
    addSprite(ed4)
    addSprite(cursor)
    
    cursor.setOrigin(0.5f, 0.5f)
    
    ed.setOrigin(0.5f, 0.5f)
    
    ed2.x = 10
    ed2.y = 10
    
    ed3.rotation = 125
    ed3.x = -200
    ed3.scaleX = -0.5f
    ed3.setOrigin(0.5f, 0f)
    
    ed4.y = -150    
    ed4.setOrigin(0.5f, 0.5f)
    ed4.scaleX = -1f
    ed4.scaleY = -1f
    
    app.inputs.addProcessor(new InputTest())
  }
  
  class InputTest extends InputAdapter {
    import com.badlogic.gdx.Input.Keys._

    override def keyUp(keycode : Int) : Boolean = {
      keycode match {
        case F5 => Screen.DEBUG = !Screen.DEBUG
        case TAB => ed.visible = !ed.visible
        case PLUS => ed.rotation += 12.0f
        case MINUS => ed.rotation -= 12.0f
        case W => ed.y += 10.0f
        case S => ed.y -= 10.0f
        case D => ed.x += 10.0f
        case A => ed.x -= 10.0f
        case U => ed.scaleX *= 1.1f
        case I => ed.scaleX /= 1.1f
        case J => ed.scaleY *= 1.1f
        case K => ed.scaleY /= 1.1f
        case O => ed.scaleX *= 1.1f; ed.scaleY *= 1.1f
        case L => ed.scaleX /= 1.1f; ed.scaleY /= 1.1f
        case _ =>
      }

      false
    }
    
    private[this] val tmp = new Vector3
    override def touchMoved(x: Int, y: Int) = {
      tmp.x = screenToViewPortX(x)
      tmp.y = screenToViewPortY(y)
      tmp.z = 0
      cam.unproject(tmp)
      cursor.x = tmp.x
      cursor.y = tmp.y
      
      false
    }
  }
}

