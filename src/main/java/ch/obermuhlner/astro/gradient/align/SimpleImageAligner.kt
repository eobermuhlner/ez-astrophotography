package ch.obermuhlner.astro.gradient.align

import ch.obermuhlner.astro.gradient.Point
import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorUtil

class SimpleImageAligner(val radiusX1: Int = 50, val radiusY1: Int = 0, val radiusX2: Int = 100, val radiusY2: Int = 100, val errorThreshold: Double = 1.01) : ImageAligner {

    constructor(radius: Int) : this(radius/2, 0, radius, radius)

    override fun align(base: DoubleImage, image: DoubleImage, center: Point, maxOffset: Int): ImageAligner.Alignment {
        if (base === image) {
            return ImageAligner.Alignment(0, 0, 0.0)
        }

        val baseCropped1 = base.croppedImage(center.x-radiusX1, center.y-radiusY1, radiusX1*2+1, radiusY1+1)
        val baseCropped2 = base.croppedImage(center.x-radiusX2, center.y-radiusY2, radiusX2*2+1, radiusY2+1)

        var bestError1 = 1.0
        var bestError2 = 1.0

        var bestAlignX = 0
        var bestAlignY = 0
        for (dy in -maxOffset .. maxOffset) {
            for (dx in -maxOffset .. maxOffset) {
                val x = center.x + dx
                val y = center.y + dy
                val error1 = differenceAverageError(baseCropped1, image.croppedImage(x-radiusX1, y-radiusY1, radiusX1*2+1, radiusY1+1))
                if (error1 < bestError1 * errorThreshold) {
                    val error2 = differenceAverageError(baseCropped2, image.croppedImage(x-radiusX2, y-radiusY2, radiusX2*2+1, radiusY2*2+1))
                    if (error2 < bestError2) {
                        bestAlignX = dx
                        bestAlignY = dy
                        bestError1 = error1
                        bestError2 = error2
                    }
                }
            }
        }

        return ImageAligner.Alignment(bestAlignX, bestAlignY, bestError2)
    }

    fun differenceAverageError(image1: DoubleImage, image2: DoubleImage): Double {
        val color1 = DoubleArray(3)
        val color2 = DoubleArray(3)
        var error = 0.0
        val n = image1.width * image1.height
        for (y in 0 until image1.height) {
            for (x in 0 until image1.width) {
                image1.getPixel(x, y, ColorModel.RGB, color1)
                image2.getPixel(x, y, ColorModel.RGB, color2)
                val lum1 = ColorUtil.convertRGBtoLuminosity(color1)
                val lum2 = ColorUtil.convertRGBtoLuminosity(color2)
                val diff = lum1 - lum2
                error += diff * diff
            }
        }
        return error / n
    }
}