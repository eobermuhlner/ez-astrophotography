package ch.obermuhlner.astro.image

import ch.obermuhlner.astro.image.color.ColorModel
import java.util.*

object ImageUtil {
    fun medianPixelPerSample(image: DoubleImage, colorModel: ColorModel, color: DoubleArray?): DoubleArray {
        var color = color
        if (color == null) {
            color = DoubleArray(3)
        }
        val n = image.width * image.height
        val values0 = DoubleArray(n)
        val values1 = DoubleArray(n)
        val values2 = DoubleArray(n)
        var index = 0
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                image.getPixel(x, y, colorModel, color)
                values0[index] = color[0]
                values1[index] = color[1]
                values2[index] = color[2]
                index++
            }
        }
        Arrays.sort(values0)
        Arrays.sort(values1)
        Arrays.sort(values2)
        if (n % 2 == 0) {
            color[0] = (values0[n / 2] + values0[n / 2 + 1]) / 2
            color[1] = (values1[n / 2] + values1[n / 2 + 1]) / 2
            color[2] = (values2[n / 2] + values2[n / 2 + 1]) / 2
        } else {
            color[0] = values0[n / 2]
            color[1] = values1[n / 2]
            color[2] = values2[n / 2]
        }
        return color
    }
}