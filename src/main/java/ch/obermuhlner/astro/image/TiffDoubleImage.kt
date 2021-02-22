package ch.obermuhlner.astro.image

import ch.obermuhlner.astro.image.color.ColorModel
import mil.nga.tiff.Rasters
import mil.nga.tiff.TIFFImage

class TiffDoubleImage(val tiffImage: TIFFImage, read: Boolean) : DoubleImage {
    val image: Rasters = if (read) tiffImage.fileDirectories[0].readRasters() else tiffImage.fileDirectories[0].writeRasters
    override val width: Int
        get() = image.width
    override val height: Int
        get() = image.height

    override fun getNativePixel(x: Int, y: Int, color: DoubleArray): DoubleArray {
        val pixel: Array<Number> = image.getPixel(x, y)
        if (pixel[0] is Double) {
            color[ColorModel.RGB.R] = pixel[0] as Double
            color[ColorModel.RGB.G] = pixel[1] as Double
            color[ColorModel.RGB.B] = pixel[2] as Double
        } else if (pixel[0] is Float) {
            color[ColorModel.RGB.R] = (pixel[0] as Float).toDouble()
            color[ColorModel.RGB.G] = (pixel[1] as Float).toDouble()
            color[ColorModel.RGB.B] = (pixel[2] as Float).toDouble()
        } else {
            color[ColorModel.RGB.R] = pixel[0].toDouble() / 256.0
            color[ColorModel.RGB.G] = pixel[1].toDouble() / 256.0
            color[ColorModel.RGB.B] = pixel[2].toDouble() / 256.0
        }
        return color
    }

    override fun setNativePixel(x: Int, y: Int, color: DoubleArray) {
        image.setPixel(x, y, arrayOf(color[ColorModel.RGB.R].toFloat(), color[ColorModel.RGB.G].toFloat(), color[ColorModel.RGB.B].toFloat()))
    }
}