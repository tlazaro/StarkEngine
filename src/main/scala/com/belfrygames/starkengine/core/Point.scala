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
  @inline def length: Double = scala.math.sqrt(num.toDouble(length2))

  @inline def toFloat: Point2D[Float] = Point2D(num.toFloat(x), num.toFloat(y))
  @inline def toDouble: Point2D[Double] = Point2D(num.toDouble(x), num.toDouble(y))
  @inline def toInt: Point2D[Int] = Point2D(num.toInt(x), num.toInt(y))
  @inline def toLong: Point2D[Long] = Point2D(num.toLong(x), num.toLong(y))

  @inline def inside(r: Rectangle[A]): Boolean = r contains this

  @inline def unapply(): Option[(A, A)] = Some((x, y))
}

case class Point3D[@specialized A: Numeric](x: A, y: A, z: A) {
  val num = implicitly[Numeric[A]]

  @inline def +(other: Point3D[A]): Point3D[A] = Point3D(num.plus(x, other.x), num.plus(y, other.y), num.plus(z, other.z))
  @inline def -(other: Point3D[A]): Point3D[A] = Point3D(num.minus(x, other.x), num.minus(y, other.y), num.minus(z, other.z))

  @inline def *(alpha: A): Point3D[A] = Point3D(num.times(x, alpha), num.times(y, alpha), num.times(z, alpha))

  @inline def length2: A = num.plus(num.plus(num.times(x, x), num.times(y, y)), num.times(z, z))
  @inline def length: Double = scala.math.sqrt(num.toDouble(length2))

  @inline def toFloat: Point3D[Float] = Point3D(num.toFloat(x), num.toFloat(y), num.toFloat(z))
  @inline def toDouble: Point3D[Double] = Point3D(num.toDouble(x), num.toDouble(y), num.toDouble(z))
  @inline def toInt: Point3D[Int] = Point3D(num.toInt(x), num.toInt(y), num.toInt(z))
  @inline def toLong: Point3D[Long] = Point3D(num.toLong(x), num.toLong(y), num.toLong(z))

  @inline def unapply(): Option[(A, A, A)] = Some((x, y, z))
}
