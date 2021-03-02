package ch.obermuhlner.astro.stack

import ch.obermuhlner.astro.image.DoubleImage

abstract class AbstractStacker : Stacker {
    override fun stack(stackingImages: List<StackingImage>, output: DoubleImage) {
        val color = DoubleArray(3)
        for (y in 0 until output.height) {
            for (x in 0 until output.width) {
                stackPixel(x, y, stackingImages, color)
                output.setPixel(x, y, color)
            }
        }

    }

    abstract fun stackPixel(x: Int, y: Int, stackingImages: List<StackingImage>, color: DoubleArray)
}