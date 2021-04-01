package ch.obermuhlner.astro.gradient.align

import ch.obermuhlner.astro.gradient.Point
import ch.obermuhlner.astro.image.DoubleImage

interface ImageAligner {

    fun align(base: DoubleImage, image: DoubleImage, center: Point = Point(base.width/2, base.height/2), maxOffset: Int = 200): Alignment

    data class Alignment(val x: Int, val y: Int, val error: Double)
}