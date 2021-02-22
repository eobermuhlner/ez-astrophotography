package ch.obermuhlner.astro.gradient

import java.util.*

class Point constructor(val x: Int, val y: Int) {
    fun distanceSquare(other: Point?): Double {
        val dx: Double = (other!!.x - x).toDouble()
        val dy: Double = (other.y - y).toDouble()
        return dx * dx + dy * dy
    }

    fun distance(other: Point?): Double {
        return Math.sqrt(distanceSquare(other))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val point: Point = other as Point
        return x == point.x &&
                y == point.y
    }

    override fun hashCode(): Int {
        return Objects.hash(x, y)
    }

    override fun toString(): String {
        return ("Point{" +
                "x=" + x +
                ", y=" + y +
                '}')
    }
}