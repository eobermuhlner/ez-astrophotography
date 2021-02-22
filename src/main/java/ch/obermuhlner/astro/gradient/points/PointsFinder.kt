package ch.obermuhlner.astro.gradient.points

import ch.obermuhlner.astro.gradient.Point

open interface PointsFinder {
    fun setFixPoints(points: Collection<Point>)
    fun getRelevantFixPoints(point: Point): List<Point>
}