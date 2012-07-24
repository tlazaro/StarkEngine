package com.belfrygames.starkengine.core

object AppTest {
  def apply(config: Config) = {
    StarkApp.app = new AppTest(config)
    StarkApp.app
  }
}

class AppTest(config0: Config) extends StarkApp (config0) {

  override def startFirstScreen() {
    Resources.loadTexturePack("ui.atlas")
    super.startFirstScreen()
  }
}
