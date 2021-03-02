package ch.obermuhlner.astro.stack

import ch.obermuhlner.astro.image.color.ColorModel
import kotlin.math.max

class MaxStacker : AbstractStacker() {
    override fun stackPixel(x: Int, y: Int, stackingImages: List<StackingImage>, color: DoubleArray) {
        color[ColorModel.RGB.R] = 0.0
        color[ColorModel.RGB.G] = 0.0
        color[ColorModel.RGB.B] = 0.0

        val tempColor = DoubleArray(3)
        for (stackingImage in stackingImages) {
            stackingImage.image.getPixel(x+stackingImage.x, y+stackingImage.y, ColorModel.RGB, tempColor)

            color[ColorModel.RGB.R] = max(color[ColorModel.RGB.R], tempColor[ColorModel.RGB.R])
            color[ColorModel.RGB.G] = max(color[ColorModel.RGB.G], tempColor[ColorModel.RGB.G])
            color[ColorModel.RGB.B] = max(color[ColorModel.RGB.B], tempColor[ColorModel.RGB.B])
        }
    }
}