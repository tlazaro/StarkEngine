package com.belfrygames.starkengine.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector3
import com.belfrygames.starkengine.tags._

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
    
    foreground.add(node, "node")
    
    val cont = new Node(){}
    node.add(cont, "labelcont")
    cont.x = -200
    
//    cont.setController(new ControllerQueue(
//        new Rotate(360.0f, tag[Milliseconds](5000L))
//      ))
    
    val label2 = new Label(new BitmapText(new BitmapFont, "Me gusta\nStark Engine\nMucho Mucho\nCacacacaca"))
    cont.add(label2, "label2")
    label2.y = -30
    label2.x = -100
    
    label2.scaleX = 2
    label2.scaleY = 2
    
    label2.setOrigin(0.5f, 0.5f)
    
    val label3 = new Label(Text.getText(15, "15 Me gusta\nStark Engine\nMucho Mucho"))
    cont.add(label3, "label3")
    label3.y = -60
    
    label3.scaleX = 2
    label3.scaleY = 2
    
    label3.setOrigin(1.0f, 1.0f)
    
    val label7 = new Label(Text.getText(32, "Me gusta\nStark Engine\nMucho Mucho\nCacacacaca"))
    cont.add(label7, "label7")
    label7.y = -240
    
    label7.scaleX = 2
    label7.scaleY = 2
    
    label7.setOrigin(0.5f, 0.5f)
    
//    label2.setController(new ControllerSet(
//        new Scale(3f, 3f, tag[Milliseconds](5000L))
//      ))
    
    val a = Sprite(app.res.get("eddard"))
    node.add(a, "a")
    
    a.setController(new ControllerQueue(
        new Rotate(90.0f, tag[Milliseconds](2000L)),
        new Rotate(-90.0f, tag[Milliseconds](2000L))
      ))
    
    val b = Sprite(app.res.get("eddard"))
    b.x = 100
    a.add(b, "b")
    
    val c = Sprite(app.res.get("eddard"))
    node.add(c, "c")
    
    b.setController(new Controller[Updateable]{
        def forceFinish() {}
        def finished(): Boolean = true
        override def update(elapsed: Long @@ Milliseconds) {
          println("WORKED!")
        }
      })
    
    c.setController(new ControllerQueue(
        new MoveTo(Point2D(200, 0), tag[Milliseconds](1000L)),
        new MoveTo(Point2D(200, 200), tag[Milliseconds](1000L)),
        new MoveTo(Point2D(0, 200), tag[Milliseconds](1000L)),
        new MoveTo(Point2D(0, 0), tag[Milliseconds](1000L)),
        new MoveTo(Point2D(200, 200), tag[Milliseconds](1000L))
      ))
    
//    addSprite(ed)
//    addSprite(ed2)
//    addSprite(ed3)
//    addSprite(ed4)
//    
    foreground.add(cursor, "cursor")
    
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

