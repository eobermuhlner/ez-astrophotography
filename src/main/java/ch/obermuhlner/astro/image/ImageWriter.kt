package ch.obermuhlner.astro.image

import ch.obermuhlner.astro.image.color.ColorModel
import mil.nga.tiff.TiffWriter
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO

object ImageWriter {
    @Throws(IOException::class)
    fun write(image: DoubleImage, output: File) {
        val name = output.name
        for (format in ImageFormat.values()) {
            for (extension in format.extensions) {
                if (name.length > extension.length && name.substring(name.length - extension.length).equals(extension, ignoreCase = true)) {
                    write(image, output, format)
                    return
                }
            }
        }
        write(image, output, ImageFormat.TIF)
    }

    @Throws(IOException::class)
    fun write(image: DoubleImage, output: File, format: ImageFormat) {
        var image = image
        if (image is TiffDoubleImage) {
            if (format == ImageFormat.TIF) {
                TiffWriter.writeTiff(output, image.tiffImage)
                return
            }
            val width = image.width
            val height = image.height
            val rgb = DoubleArray(3)
            val imageCopy = ImageCreator.create(width, height, ImageQuality.Standard)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    image.getPixel(x, y, ColorModel.RGB, rgb)
                    imageCopy.setPixel(x, y, ColorModel.RGB, rgb)
                }
            }
            image = imageCopy
        }
        if (image !is AwtBufferedDoubleImage) {
            val temp: DoubleImage = AwtBufferedDoubleImage(BufferedImage(
                    image.width,
                    image.height,
                    BufferedImage.TYPE_INT_RGB
            ))
            temp.setPixels(0, 0, image, 0, 0, image.width, image.height, ColorModel.RGB, null)
            image = temp
        }
        val bufferedDoubleImage = image as AwtBufferedDoubleImage
        ImageIO.write(bufferedDoubleImage.image, format.name, output)
    }
}