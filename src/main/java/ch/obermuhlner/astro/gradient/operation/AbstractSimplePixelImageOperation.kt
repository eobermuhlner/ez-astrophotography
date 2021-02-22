package ch.obermuhlner.astro.gradient.operation

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel

abstract class AbstractSimplePixelImageOperation : ImageOperation {
    override fun operation(image1: DoubleImage, image2: DoubleImage, result: DoubleImage): DoubleImage {
        val pixel1: DoubleArray = DoubleArray(3)
        val pixel2: DoubleArray = DoubleArray(3)
        val pixelResult: DoubleArray = DoubleArray(3)
        val colorModel: ColorModel = result.colorModel
        for (y in 0 until image1.height) {
            for (x in 0 until image1.width) {
                image1.getPixel(x, y, colorModel, pixel1)
                image2.getPixel(x, y, colorModel, pixel2)
                result.setPixel(x, y, colorModel, pixelOperation(pixel1, pixel2, x, y, pixelResult))
            }
        }
        return result
    }

    protected abstract fun pixelOperation(pixel1: DoubleArray, pixel2: DoubleArray, x: Int, y: Int, result: DoubleArray): DoubleArray
}