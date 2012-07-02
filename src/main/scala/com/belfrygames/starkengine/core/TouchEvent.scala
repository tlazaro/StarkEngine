package com.belfrygames.starkengine.core

object TouchEvent extends Enumeration {
  type Event = Value
  val Empty, Entered, Exited = Value
  
  var over: Option[Node] = None
}
