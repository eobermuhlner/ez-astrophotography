package ch.obermuhlner.astro.gradient.filter

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel

abstract class AbstractFilter @JvmOverloads constructor(private val defaultColorModel: ColorModel? = null) : Filter {
    override fun filter(source: DoubleImage, target: DoubleImage, width: Int, height: Int): DoubleImage {
        val colorModel = defaultColorModel ?: target.colorModel
        val color = DoubleArray(3)
        for (y in 0 until height) {
            for (x in 0 until width) {
                target.setPixel(x, y, colorModel, filterPixel(source, x, y, colorModel, color))
            }
        }
        return target
    }

    protected abstract fun filterPixel(source: DoubleImage, x: Int, y: Int, colorModel: ColorModel, color: DoubleArray): DoubleArray
}