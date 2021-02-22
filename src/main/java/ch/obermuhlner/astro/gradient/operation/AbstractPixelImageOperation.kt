package ch.obermuhlner.astro.gradient.operation

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel

abstract class AbstractPixelImageOperation : ImageOperation {
    override fun operation(image1: DoubleImage, image2: DoubleImage, result: DoubleImage): DoubleImage {
        val samples = DoubleArray(3)
        val colorModel: ColorModel = result.colorModel
        for (y in 0 until image1.height) {
            for (x in 0 until image2.width) {
                val pixelResult: DoubleArray = pixelOperation(image1, image2, x, y, samples)
                result.setPixel(x, y, colorModel, pixelResult)
            }
        }
        return result
    }

    protected abstract fun pixelOperation(image1: DoubleImage, image2: DoubleImage, x: Int, y: Int, result: DoubleArray?): DoubleArray
}