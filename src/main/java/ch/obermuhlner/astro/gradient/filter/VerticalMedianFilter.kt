package ch.obermuhlner.astro.gradient.filter

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import java.util.*

class VerticalMedianFilter constructor(private val radius: Int, private val model: ColorModel) : Filter {
    override fun filter(source: DoubleImage, target: DoubleImage, width: Int, height: Int): DoubleImage {
        val data: Array<DoubleArray> = Array(height) { DoubleArray(3) }
        for (x in 0 until width) {
            for (y in 0 until height) {
                var n = 0
                for (dy in -radius..radius) {
                    val yy: Int = y + dy
                    if (source.isValidPixel(x, yy)) {
                        source.getPixel(x, yy, model, data[n++])
                    }
                }
                Arrays.sort(data, 0, n,
                        Comparator.comparingDouble { c: DoubleArray -> c[2] }
                        .thenComparing { c: DoubleArray -> c[1] }
                        .thenComparing { c: DoubleArray -> c[0] })
                target.setPixel(x, y, model, data[n / 2])
            }
        }
        return target
    }

    override fun toString(): String {
        return "VerticalMedianFilter(radius=$radius)"
    }
}