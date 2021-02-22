package ch.obermuhlner.astro.gradient.filter

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel

class CopyFilter constructor(private val model: ColorModel) : Filter {
    override fun filter(source: DoubleImage, target: DoubleImage, width: Int, height: Int): DoubleImage {
        val samples = DoubleArray(3)
        for (dy in 0 until height) {
            for (dx in 0 until width) {
                if (source.isInside(dx, dy)) {
                    source.getPixel(dx, dy, model, samples)
                } else {
                    samples[0] = 0.0
                    samples[1] = 0.0
                    samples[2] = 0.0
                }
                target.setPixel(dx, dy, model, samples)
            }
        }
        return target
    }

    override fun toString(): String {
        return "CopyFilter"
    }
}