package cz.payola.web.client.views

import scala.math._
import s2js.adapters.js.browser.window

/**
  * Representation of a vector in 2-dimensional space.
  * @param x first value of the vector
  * @param y first value of the vector
  */
case class Vector2D(x: Double, y: Double)
{
    /**
      * Addition
      * @param vector to add
      * @return new vector with values of this vector plus the parameter vector
      */
    def +(vector: Vector2D): Vector2D = {
        Vector2D(x + vector.x, y + vector.y)
    }

    /**
      * Deduction
      * @param vector to deduct
      * @return new vector with values of this vector minus the parameter vector
      */
    def -(vector: Vector2D): Vector2D = {
        Vector2D(x - vector.x, y - vector.y)
    }

    /**
      * Times -1
      * @return new vector with values of this vector times -1
      */
    def unary_-(): Vector2D = {
        Vector2D(-x, -y)
    }

    /**
      * Multiplication
      * @param value to multiply this vectors values with
      * @return new vector with values multiplied by the parameter value
      */
    def *(value: Double): Vector2D = {
        Vector2D(x * value, y * value)
    }

    /**
      * Division
      * @param value to divide this vectors values with
      * @return new vector with values divided by the parameter value
      */
    def /(value: Double): Vector2D = {
        Vector2D(x / value, y / value)
    }

    /**
      * Counts length of this vector.
      * @return square root of added squares of values of this vector
      */
    def length: Double = {
        sqrt(pow(x, 2) + pow(y, 2))
    }

    override def toString: String = {
        "[" + x + ", " + y + "]"
    }
}

/**
  * Frequently used constant vectors
  */
object Vector2D
{
    val Zero = Vector2D(0, 0)

    val One = Vector2D(1, 1)
}