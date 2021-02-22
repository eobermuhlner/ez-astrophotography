package ch.obermuhlner.astro.image

import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorUtil
import java.awt.image.BufferedImage

class AwtBufferedDoubleImage(val image: BufferedImage) : DoubleImage {
    override val width: Int
        get() = image.width
    override val height: Int
        get() = image.height

    override fun getNativePixel(x: Int, y: Int, color: DoubleArray?): DoubleArray {
        var color = color
        if (color == null) {
            color = DoubleArray(3)
        }
        val rgb = image.getRGB(x, y)
        color[ColorModel.RGB.R] = (rgb shr 16 and 0xff) / 255.0
        color[ColorModel.RGB.G] = (rgb shr 8 and 0xff) / 255.0
        color[ColorModel.RGB.B] = (rgb and 0xff) / 255.0
        return color
    }

    override fun setNativePixel(x: Int, y: Int, color: DoubleArray) {
        image.setRGB(x, y, ColorUtil.toIntRGB(color))
    }
}