package ch.obermuhlner.astro.javafx

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorModel.RGB
import ch.obermuhlner.astro.image.color.ColorUtil
import javafx.scene.image.WritableImage

class JavaFXWritableDoubleImage constructor(private val image: WritableImage) : DoubleImage {
    override val width: Int
        get() {
            return image.getWidth().toInt()
        }
    override val height: Int
        get() {
            return image.getHeight().toInt()
        }

    public override fun getNativePixel(x: Int, y: Int, color: DoubleArray?): DoubleArray {
        var color: DoubleArray? = color
        if (color == null) {
            color = DoubleArray(3)
        }
        val rgb: Int = image.getPixelReader().getArgb(x, y)
        color[ColorModel.RGB.R] = ((rgb shr 16) and 0xff) / 255.0
        color[ColorModel.RGB.G] = ((rgb shr 8) and 0xff) / 255.0
        color[ColorModel.RGB.B] = (rgb and 0xff) / 255.0
        return color
    }

    public override fun setNativePixel(x: Int, y: Int, color: DoubleArray) {
        image.getPixelWriter().setArgb(x, y, ColorUtil.toIntARGB(color))
    }
}