package ch.obermuhlner.astro.image

import ch.obermuhlner.astro.image.color.ColorModel

class ArrayDoubleImage(override val width: Int, override val height: Int, override val colorModel: ColorModel) : DoubleImage {
    private val data = DoubleArray(width * height * SAMPLES_PER_PIXEL)

    override fun getNativePixel(x: Int, y: Int, color: DoubleArray): DoubleArray {
        val index = (x + y * width) * SAMPLES_PER_PIXEL
        color[0] = data[index + 0]
        color[1] = data[index + 1]
        color[2] = data[index + 2]
        return color
    }

    override fun setNativePixel(x: Int, y: Int, color: DoubleArray) {
        val index = (x + y * width) * SAMPLES_PER_PIXEL
        data[index + 0] = color[0]
        data[index + 1] = color[1]
        data[index + 2] = color[2]
    }

    companion object {
        private const val SAMPLES_PER_PIXEL = 3
    }
}