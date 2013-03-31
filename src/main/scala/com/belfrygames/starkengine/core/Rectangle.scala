package com.belfrygames.starkengine.core

object Rectangle {
  val EMPTY_INT = Rectangle(0, 0, 0, 0)
  val EMPTY_LONG = Rectangle(0L, 0L, 0L, 0L)
  val EMPTY_FLOAT = Rectangle(0f, 0f, 0f, 0f)
  val EMPTY_DOUBLE = Rectangle(0.0, 0.0, 0.0, 0.0)
}

case class Rectangle[@specialized A: Numeric](x0: A, y0: A, x1: A, y1: A) {
  val num = implicitly[Numeric[A]]

  @inline def join(other: Rectangle[A]): Rectangle[A] = Rectangle(
    num.min(num.min(x0, x1), num.min(other.x0, other.x1)),
    num.min(num.min(y0, y1), num.min(other.y0, other.y1)),
    num.max(num.max(x0, x1), num.max(other.x0, other.x1)),
    num.max(num.max(y0, y1), num.max(other.y0, other.y1)))

  @inline def area: A = num.times(num.abs(num.minus(x1, x0)), num.abs(num.minus(y1, y0)))
  @inline def width: A = num.minus(x1, x0)
  @inline def height: A = num.minus(y1, y0)

  @inline def center: Point2D[A] = {
    val two = num.plus(num.one, num.one)
    val x = num.plus(x0, x1)
    val y = num.plus(y0, y1)
    num match {
      case f: Fractional[A] => Point2D(f.div(x, two), f.div(y, two))
      case i: Integral[A] => Point2D(i.quot(x, two), i.quot(y, two))
      case _ => sys.error("Undivisable numeric!")
    }
  }

  @inline def move(p: Point2D[A]): Rectangle[A] = move(p.x, p.y)
  @inline def move(px: A, py: A): Rectangle[A] = Rectangle(num.plus(x0, px), num.plus(y0, py), num.plus(x1, px), num.plus(y1, py))

  @inline def contains(p: Point2D[A]): Boolean = contains(p.x, p.y)
  @inline def contains(px: A, py: A): Boolean = {
    (if (num.lteq(x0, x1)) (num.lteq(x0, px) && num.lteq(px, x1)) else (num.lteq(x1, px) && num.lteq(px, x0))) &&
      (if (num.lteq(y0, y1)) (num.lteq(y0, py) && num.lteq(py, y1)) else (num.lteq(y1, py) && num.lteq(py, y0)))
  }

  @inline def toFloat: Rectangle[Float] = Rectangle(num.toFloat(x0), num.toFloat(y0), num.toFloat(x1), num.toFloat(y1))
  @inline def toDouble: Rectangle[Double] = Rectangle(num.toDouble(x0), num.toDouble(y0), num.toDouble(x1), num.toDouble(y1))
  @inline def toInt: Rectangle[Int] = Rectangle(num.toInt(x0), num.toInt(y0), num.toInt(x1), num.toInt(y1))
  @inline def toLong: Rectangle[Long] = Rectangle(num.toLong(x0), num.toLong(y0), num.toLong(x1), num.toLong(y1))
}