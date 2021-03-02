package ch.obermuhlner.astro.stack

import ch.obermuhlner.astro.image.color.ColorModel

class AverageStacker : AbstractStacker() {
    override fun stackPixel(x: Int, y: Int, stackingImages: List<StackingImage>, color: DoubleArray) {
        color[ColorModel.RGB.R] = 0.0
        color[ColorModel.RGB.G] = 0.0
        color[ColorModel.RGB.B] = 0.0

        val n = stackingImages.size.toDouble()
        val tempColor = DoubleArray(3)
        for (stackingImage in stackingImages) {
            stackingImage.image.getPixel(x+stackingImage.x, y+stackingImage.y, ColorModel.RGB, tempColor)

            color[ColorModel.RGB.R] += tempColor[ColorModel.RGB.R]
            color[ColorModel.RGB.G] += tempColor[ColorModel.RGB.G]
            color[ColorModel.RGB.B] += tempColor[ColorModel.RGB.B]
        }

        color[ColorModel.RGB.R] /= n
        color[ColorModel.RGB.G] /= n
        color[ColorModel.RGB.B] /= n
    }
}