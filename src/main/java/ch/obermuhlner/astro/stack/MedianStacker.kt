package ch.obermuhlner.astro.stack

import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorUtil

class MedianStacker : AbstractSortedStacker() {

    override fun stackHSVtoRGB(sortedHSV: Array<DoubleArray>, rgb: DoubleArray) {
        val n = sortedHSV.size
        val nHalf = n / 2
        val nHalfPlus1 = nHalf + 1
        if (n % 2 == 0) {
            rgb[0] = (sortedHSV[nHalf][ColorModel.HSV.H] + sortedHSV[nHalfPlus1][ColorModel.HSV.H]) / 2
            rgb[1] = (sortedHSV[nHalf][ColorModel.HSV.S] + sortedHSV[nHalfPlus1][ColorModel.HSV.S]) / 2
            rgb[2] = (sortedHSV[nHalf][ColorModel.HSV.V] + sortedHSV[nHalfPlus1][ColorModel.HSV.V]) / 2
        } else {
            rgb[0] = sortedHSV[nHalf][ColorModel.HSV.H]
            rgb[1] = sortedHSV[nHalf][ColorModel.HSV.S]
            rgb[2] = sortedHSV[nHalf][ColorModel.HSV.V]
        }
        ColorUtil.convert(ColorModel.HSV, rgb, ColorModel.RGB, rgb)
    }
}