package ch.obermuhlner.astro.gradient.points

import ch.obermuhlner.astro.gradient.Point
import io.github.jdiemke.triangulation.DelaunayTriangulator
import io.github.jdiemke.triangulation.NotEnoughPointsException
import io.github.jdiemke.triangulation.Triangle2D
import io.github.jdiemke.triangulation.Vector2D
import java.util.*

class VoronoiPointsFinder : PointsFinder {
    private val points: MutableList<Point> = ArrayList()
    private var delaunayPoints: List<Vector2D>? = null
    private var delaunayTriangles: List<Triangle2D>? = null
    private var delaunayMapToPoints: MutableMap<Vector2D, Point> = HashMap()

    override fun setFixPoints(points: Collection<Point>) {
        this.points.clear()
        this.points.addAll(points)
        triangulate(this.points)
    }

    private fun triangulate(points: List<Point>) {
        if (points.size < 3) {
            delaunayPoints = null
            delaunayTriangles = null
            return
        }
        val vectors: MutableList<Vector2D> = ArrayList()
        for (point: Point in points) {
            val v = Vector2D(point.x.toDouble(), point.y.toDouble())
            delaunayMapToPoints[v] = point
            vectors.add(v)
        }
        val triangulator = DelaunayTriangulator(vectors)
        try {
            triangulator.triangulate()
        } catch (e: NotEnoughPointsException) {
            throw RuntimeException(e)
        }
        delaunayPoints = triangulator.pointSet
        delaunayTriangles = triangulator.triangles
    }

    override fun getRelevantFixPoints(point: Point): List<Point> {
        val delaunayPolygon: MutableSet<Vector2D> = HashSet()
        if (points.size <= 3) {
            return points
        }
        val vector = Vector2D(point.x.toDouble(), point.y.toDouble())
        var closestDelaunayPoint: Vector2D? = null
        var closestDistance: Double = Double.MAX_VALUE
        for (delaunayPoint: Vector2D? in delaunayPoints!!) {
            val distance: Double = vector.sub(delaunayPoint).mag()
            if (closestDelaunayPoint == null || distance < closestDistance) {
                closestDelaunayPoint = delaunayPoint
                closestDistance = distance
            }
        }
        for (triangle: Triangle2D in delaunayTriangles!!) {
            if (triangle.hasVertex(closestDelaunayPoint)) {
                delaunayPolygon.add(triangle.a)
                delaunayPolygon.add(triangle.b)
                delaunayPolygon.add(triangle.c)
            }
        }
        val result: MutableList<Point> = ArrayList()
        for (vector2D: Vector2D in delaunayPolygon) {
            result.add(delaunayMapToPoints[vector2D]!!)
        }
        return result
    }
}