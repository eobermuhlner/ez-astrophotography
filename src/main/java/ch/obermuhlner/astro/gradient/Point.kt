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

    public override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val point: Point = o as Point
        return x == point.x &&
                y == point.y
    }

    public override fun hashCode(): Int {
        return Objects.hash(x, y)
    }

    public override fun toString(): String {
        return ("Point{" +
                "x=" + x +
                ", y=" + y +
                '}')
    }
}