package ch.obermuhlner.astro.gradient.points

import ch.obermuhlner.astro.gradient.Point
import java.util.*

class AllPointsFinder : PointsFinder {
    private val points: MutableList<Point> = ArrayList()

    override fun setFixPoints(points: Collection<Point>) {
        this.points.clear()
        this.points.addAll(points)
    }

    override fun getRelevantFixPoints(point: Point): List<Point> {
        return points
    }
}