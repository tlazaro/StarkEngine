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
  
  val node = new Node(){}
  val target = node
  
  override def create(app: StarkApp) {
    super.create(app)
    
    Screen.DEBUG = true
    
    add(node, "node")
    
    val a = Sprite(app.res.get("eddard"))
    node.add(a, "a")
    
    val b = Sprite(app.res.get("eddard"))
    b.x = 100
    a.add(b, "b")
    
    val c = Sprite(app.res.get("eddard"))
    c.y = 50
    node.add(c, "c")
    
//    addSprite(ed)
//    addSprite(ed2)
//    addSprite(ed3)
//    addSprite(ed4)
//    
    add(cursor, "cursor")
    
    cursor.setOrigin(0.5f, 0.5f)
    
//    ed.setOrigin(0.5f, 0.5f)
//    
//    ed2.x = 10
//    ed2.y = 10
//    
//    ed3.rotation = 125
//    ed3.x = -200
//    ed3.scaleX = -0.5f
//    ed3.setOrigin(0.5f, 0f)
//    
//    ed4.y = -150    
//    ed4.setOrigin(0.5f, 0.5f)
//    ed4.scaleX = -1f
//    ed4.scaleY = -1f
    
    app.inputs.addProcessor(new InputTest())
  }
  
  class InputTest extends InputAdapter {
    import com.badlogic.gdx.Input.Keys._

    override def keyUp(keycode : Int) : Boolean = {
      keycode match {
        case F5 => Screen.DEBUG = !Screen.DEBUG
        case TAB => target.visible = !target.visible
        case PLUS => target.rotation += 12.0f
        case MINUS => target.rotation -= 12.0f
        case W => target.y += 10.0f
        case S => target.y -= 10.0f
        case D => target.x += 10.0f
        case A => target.x -= 10.0f
        case U => target.scaleX *= 1.1f
        case I => target.scaleX /= 1.1f
        case J => target.scaleY *= 1.1f
        case K => target.scaleY /= 1.1f
        case O => target.scaleX *= 1.1f; target.scaleY *= 1.1f
        case L => target.scaleX /= 1.1f; target.scaleY /= 1.1f
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

