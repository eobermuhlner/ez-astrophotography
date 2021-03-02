package ch.obermuhlner.astro.stack

import ch.obermuhlner.astro.image.DoubleImage

class StackingImage(val image: DoubleImage, val x: Int, val y: Int)

interface Stacker {
    fun stack(stackingImages: List<StackingImage>, output: DoubleImage)
}