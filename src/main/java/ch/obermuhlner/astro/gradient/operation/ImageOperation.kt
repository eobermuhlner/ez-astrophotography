package ch.obermuhlner.astro.gradient.operation

import ch.obermuhlner.astro.image.ArrayDoubleImage
import ch.obermuhlner.astro.image.DoubleImage

open interface ImageOperation {
    fun operation(image1: DoubleImage, image2: DoubleImage, result: DoubleImage): DoubleImage
    fun operation(image1: DoubleImage, image2: DoubleImage): DoubleImage {
        return operation(image1, image2, ArrayDoubleImage(image1.width, image1.height, image1.colorModel))
    }
}