package ch.obermuhlner.astro.image

import ch.obermuhlner.astro.image.color.ColorModel
import kotlin.math.max
import kotlin.math.min

class CroppedDoubleImage(private val image: DoubleImage, private val offsetX: Int, private val offsetY: Int, override val width: Int, override val height: Int, val strictClipping: Boolean = true) : DoubleImage {
    override val colorModel: ColorModel
        get() = image.colorModel

    override fun isInsideUnderlying(x: Int, y: Int): Boolean {
        return image.isInside(x + offsetX, y + offsetY)
    }

    override fun isValidPixel(x: Int, y: Int): Boolean {
        return if (strictClipping) {
            isInsideUnderlying(x, y)
        } else {
            super.isValidPixel(x, y)
        }
    }

    override fun getNativePixel(x: Int, y: Int, color: DoubleArray): DoubleArray {
        val xx = max(0, min(image.width - 1, x + offsetX))
        val yy = max(0, min(image.height - 1, y + offsetY))
        return image.getNativePixel(xx, yy, color)
    }

    override fun setNativePixel(x: Int, y: Int, color: DoubleArray) {
        val xx = x + offsetX
        val yy = y + offsetY
        if (image.isInside(xx, yy)) {
            image.setNativePixel(x + offsetX, y + offsetX, color)
        }
    }
}