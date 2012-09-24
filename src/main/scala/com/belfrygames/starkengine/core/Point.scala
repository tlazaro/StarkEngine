package com.belfrygames.starkengine.core

object Point2D {
  val ZERO_INT = Point2D(0, 0)
  val ZERO_LONG = Point2D(0L, 0L)
  val ZERO_FLOAT = Point2D(0f, 0f)
  val ZERO_DOUBLE = Point2D(0.0, 0.0)
}

case class Point2D[@specialized A: Numeric](x: A, y: A) {
  val num = implicitly[Numeric[A]]

  @inline def +(other: Point2D[A]): Point2D[A] = Point2D(num.plus(x, other.x), num.plus(y, other.y))
  @inline def -(other: Point2D[A]): Point2D[A] = Point2D(num.minus(x, other.x), num.minus(y, other.y))

  @inline def *(alpha: A): Point2D[A] = Point2D(num.times(x, alpha), num.times(y, alpha))

  @inline def length2: A = num.plus(num.times(x, x), num.times(y, y))
  @inline def length: Double = scala.math.sqrt(num.toDouble(num.plus(num.times(x, x), num.times(y, y))))

  @inline def toFloat: Point2D[Float] = Point2D(num.toFloat(x), num.toFloat(y))
  @inline def toDouble: Point2D[Double] = Point2D(num.toDouble(x), num.toDouble(y))
  @inline def toInt: Point2D[Int] = Point2D(num.toInt(x), num.toInt(y))
  @inline def toLong: Point2D[Long] = Point2D(num.toLong(x), num.toLong(y))

  @inline def inside(r: Rectangle[A]): Boolean = r contains this
}

case class Point3D[@specialized A](x: A, y: A, z: A)
