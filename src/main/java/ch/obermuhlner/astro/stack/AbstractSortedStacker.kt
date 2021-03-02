package ch.obermuhlner.astro.stack

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorUtil
import java.util.*

abstract class AbstractSortedStacker : Stacker {
    override fun stack(stackingImages: List<StackingImage>, output: DoubleImage) {
        val colors = Array(stackingImages.size) { i -> DoubleArray(4) }
        val rgb = DoubleArray(3)

        for (y in 0 until output.height) {
            for (x in 0 until output.width) {
                for (i in stackingImages.indices) {
                    val stackingImage = stackingImages[i]
                    stackingImage.image.getPixel(x + stackingImage.x, y + stackingImage.y, ColorModel.RGB, colors[i])
                    colors[i][3] = ColorUtil.convertRGBtoLuminosity(colors[i])
                }
                Arrays.sort(colors, Comparator.comparingDouble { c: DoubleArray -> c[3] })

                stackSortedColors(colors, rgb)
                output.setPixel(x, y, rgb)
            }
        }

    }

    abstract fun stackSortedColors(sortedColors: Array<DoubleArray>, rgb: DoubleArray)
}