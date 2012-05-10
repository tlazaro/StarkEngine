package com.belfrygames.starkengine.map

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.belfrygames.starkengine.core.Region

class Tile(region: TextureRegion, val id: String, val moveCost: Int = 1, val defense: Int = 0) extends Region(region) {
}
