package ch.obermuhlner.astro.gradient.filter

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel

class MedianFilter(private val radius: Int) : AbstractFilter() {
    override fun filterPixel(source: DoubleImage, x: Int, y: Int, colorModel: ColorModel, color: DoubleArray): DoubleArray {
        val size: Int = radius + radius + 1
        source.medianPixel(x - radius, y - radius, size, size, colorModel, color)
        return color
    }

    override fun toString(): String {
        return "MedianFilter(radius=$radius)"
    }
}