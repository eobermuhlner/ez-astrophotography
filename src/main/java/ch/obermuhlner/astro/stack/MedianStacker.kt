package ch.obermuhlner.astro.stack

import ch.obermuhlner.astro.image.color.ColorModel

class MedianStacker : AbstractSortedStacker() {

    override fun stackSortedColors(sortedColors: Array<DoubleArray>, rgb: DoubleArray) {
        val n = sortedColors.size
        val nHalf = n / 2
        val nHalfPlus1 = nHalf + 1
        if (n % 2 == 0) {
            rgb[0] = (sortedColors[nHalf][ColorModel.HSV.H] + sortedColors[nHalfPlus1][ColorModel.HSV.H]) / 2
            rgb[1] = (sortedColors[nHalf][ColorModel.HSV.S] + sortedColors[nHalfPlus1][ColorModel.HSV.S]) / 2
            rgb[2] = (sortedColors[nHalf][ColorModel.HSV.V] + sortedColors[nHalfPlus1][ColorModel.HSV.V]) / 2
        } else {
            rgb[0] = sortedColors[nHalf][ColorModel.HSV.H]
            rgb[1] = sortedColors[nHalf][ColorModel.HSV.S]
            rgb[2] = sortedColors[nHalf][ColorModel.HSV.V]
        }
    }
}