package ch.obermuhlner.astro.gradient.points

import ch.obermuhlner.astro.gradient.Point
import java.util.*
import kotlin.math.min

class NearestPointsFinder constructor(private val n: Int) : PointsFinder {
    private val points: MutableList<Point> = ArrayList()

    override fun setFixPoints(points: Collection<Point>) {
        this.points.clear()
        this.points.addAll(points)
    }

    override fun getRelevantFixPoints(point: Point): List<Point> {
        points.sortWith(Comparator.comparingDouble { p: Point? -> point.distanceSquare(p) })
        val result: MutableList<Point> = ArrayList(n)
        for (i in 0 until min(n, points.size)) {
            result.add(points[i])
        }
        return result
    }
}