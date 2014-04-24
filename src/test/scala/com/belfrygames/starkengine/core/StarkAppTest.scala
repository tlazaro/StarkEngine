package com.belfrygames.starkengine.core

import org.scalatest.FunSuite
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import java.awt.Dimension
import java.awt.Toolkit

class StarkAppTest extends FunSuite {
  val FULLSCREEN = false
  
  test("Main Application Works") {
    val config = new LwjglApplicationConfiguration
    config.useGL30 = true

    // Get the default toolkit
    val scrnsize = if (FULLSCREEN) {
      val toolkit = Toolkit.getDefaultToolkit()
      toolkit.getScreenSize()
    } else {
      new Dimension(1024, 640)
    }
    
    config.width = scrnsize.width
    config.height = scrnsize.height
    
    config.title = "Application Test"
    config.fullscreen = FULLSCREEN
    config.vSyncEnabled = true
    
    val resources = new Resources() {
      def initialize() {
        set("eddard_over", Resources.load("com/belfrygames/starkengine/eddard_over.png"))
        set("eddard", Resources.load("com/belfrygames/starkengine/eddard.png"))
        set("cursor", Resources.load("com/belfrygames/starkengine/cursor.png"))
        set("death", Resources.load("com/belfrygames/starkengine/death.png"))
      }
    }
    
    val app = new LwjglApplication(AppTest(new Config(resources, new ScreenTest())), config)

    Thread.sleep(20000)
  }
}