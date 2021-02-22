package ch.obermuhlner.astro.gradient.filter

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import java.util.*
import java.util.function.Function
import java.util.function.ToDoubleFunction

class HorizontalMedianFilter constructor(private val radius: Int, private val model: ColorModel) : Filter {
    override fun filter(source: DoubleImage, target: DoubleImage, width: Int, height: Int): DoubleImage {
        val data: Array<DoubleArray> = Array(width) { DoubleArray(3) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                var n = 0
                for (dx in -radius..radius) {
                    val xx: Int = x + dx
                    if (source.isReallyInside(xx, y)) {
                        source.getPixel(xx, y, model, data.get(n++))
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
        return "HorizontalMedianFilter(radius=$radius)"
    }
}