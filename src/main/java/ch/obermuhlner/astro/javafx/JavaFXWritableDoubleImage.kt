package ch.obermuhlner.astro.javafx

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorModel.RGB
import ch.obermuhlner.astro.image.color.ColorUtil
import javafx.scene.image.WritableImage

class JavaFXWritableDoubleImage constructor(private val image: WritableImage) : DoubleImage {
    override val width: Int
        get() {
            return image.width.toInt()
        }
    override val height: Int
        get() {
            return image.height.toInt()
        }

    override fun getNativePixel(x: Int, y: Int, color: DoubleArray): DoubleArray {
        val rgb: Int = image.getPixelReader().getArgb(x, y)
        color[ColorModel.RGB.R] = ((rgb shr 16) and 0xff) / 255.0
        color[ColorModel.RGB.G] = ((rgb shr 8) and 0xff) / 255.0
        color[ColorModel.RGB.B] = (rgb and 0xff) / 255.0
        return color
    }

    override fun setNativePixel(x: Int, y: Int, color: DoubleArray) {
        image.getPixelWriter().setArgb(x, y, ColorUtil.toIntARGB(color))
    }
}