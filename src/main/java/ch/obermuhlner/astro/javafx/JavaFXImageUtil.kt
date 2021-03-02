package ch.obermuhlner.astro.javafx

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorUtil
import javafx.scene.image.WritableImage

object JavaFXImageUtil {

    fun createWritableImage(image: DoubleImage): WritableImage {
        val result = WritableImage(image.width, image.height)
        val rgb = DoubleArray(3)
        val pw = result.pixelWriter
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                image.getPixel(x, y, ColorModel.RGB, rgb)
                pw.setArgb(x, y, ColorUtil.toIntARGB(rgb))
            }
        }
        return result
    }
}