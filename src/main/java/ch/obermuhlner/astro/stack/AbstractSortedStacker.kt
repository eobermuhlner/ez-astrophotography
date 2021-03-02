package ch.obermuhlner.astro.stack

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import java.util.*

abstract class AbstractSortedStacker : Stacker {
    override fun stack(stackingImages: List<StackingImage>, output: DoubleImage) {
        val colorsHSV = Array(stackingImages.size) { i -> DoubleArray(3) }
        val rgb = DoubleArray(3)

        for (y in 0 until output.height) {
            for (x in 0 until output.width) {
                for (i in stackingImages.indices) {
                    val stackingImage = stackingImages[i]
                    stackingImage.image.getPixel(x + stackingImage.x, y + stackingImage.y, ColorModel.HSV, colorsHSV[i])
                }
                Arrays.sort(colorsHSV,
                        Comparator.comparingDouble { c: DoubleArray -> c[ColorModel.HSV.V] }
                                .thenComparing { c: DoubleArray -> c[ColorModel.HSV.S] }
                                .thenComparing { c: DoubleArray -> c[ColorModel.HSV.H] })

                stackHSVtoRGB(colorsHSV, rgb)
                output.setPixel(x, y, rgb)
            }
        }

    }

    abstract fun stackHSVtoRGB(sortedHSV: Array<DoubleArray>, rgb: DoubleArray)
}