package ch.obermuhlner.astro.gradient.filter

import ch.obermuhlner.astro.image.ArrayDoubleImage
import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel

// http://blog.ivank.net/fastest-gaussian-blur.html
class GaussianBlurFilter constructor(private val radius: Int, private val model: ColorModel) : Filter {
    override fun filter(source: DoubleImage, target: DoubleImage, width: Int, height: Int): DoubleImage {
        val temp = SwapImage(
                ArrayDoubleImage(width, height, model),
                ArrayDoubleImage(width, height, model)
        )
        temp.source.setPixels(source, model, null)
        val boxSizes: DoubleArray = boxSizesForGauss(radius.toDouble(), 3)
        for (boxSize: Double in boxSizes) {
            val boxRadius: Int = (Math.ceil((boxSize - 1) / 2) + 0.5).toInt()
            boxBlur(temp.source, temp.target, width, height, boxRadius)
            temp.swap()
        }
        target.setPixels(temp.source, model, null)
        return target
    }

    private class SwapImage(var source: DoubleImage, var target: DoubleImage) {
        fun swap() {
            val tmp: DoubleImage = source
            source = target
            target = tmp
        }
    }

    private fun boxBlur(source: DoubleImage, target: DoubleImage, width: Int, height: Int, boxRadius: Int) {
        val tempImage = ArrayDoubleImage(width, height, model)
        boxBlurHorizontal(source, tempImage, width, height, boxRadius)
        boxBlurVertical(tempImage, target, width, height, boxRadius)
    }

    private fun boxBlurHorizontal(source: DoubleImage, target: DoubleImage, width: Int, height: Int, boxRadius: Int) {
        val kernelSize = boxRadius + boxRadius + 1
        val color = DoubleArray(3)

        for (y in 0 until height) {
            source.getPixel(0, y, model, color)
            val first0 = color[0]
            val first1 = color[1]
            val first2 = color[2]

            source.getPixel(width - 1, y, model, color)
            val last0 = color[0]
            val last1 = color[1]
            val last2 = color[2]

            var leftX = 0
            var rightX = boxRadius
            var targetX = 0

            var sum0 = first0 * (boxRadius + 1)
            var sum1 = first1 * (boxRadius + 1)
            var sum2 = first2 * (boxRadius + 1)
            for (x in 0 until boxRadius) {
                source.getPixel(x, y, model, color)
                sum0 += color[0]
                sum1 += color[1]
                sum2 += color[2]
            }
            for (x in 0..boxRadius) {
                source.getPixel(rightX++, y, model, color)
                sum0 += color[0] - first0
                sum1 += color[1] - first1
                sum2 += color[2] - first2

                color[0] = sum0 / kernelSize
                color[1] = sum1 / kernelSize
                color[2] = sum2 / kernelSize
                target.setPixel(targetX++, y, model, color)
            }
            for (x in boxRadius + 1 until width - boxRadius) {
                source.getPixel(rightX++, y, model, color)
                sum0 += color[0]
                sum1 += color[1]
                sum2 += color[2]

                source.getPixel(leftX++, y, model, color)
                sum0 -= color[0]
                sum1 -= color[1]
                sum2 -= color[2]

                color[0] = sum0 / kernelSize
                color[1] = sum1 / kernelSize
                color[2] = sum2 / kernelSize
                target.setPixel(targetX++, y, model, color)
            }
            for (x in width - boxRadius until width) {
                sum0 += last0
                sum1 += last1
                sum2 += last2

                source.getPixel(leftX++, y, model, color)
                sum0 -= color[0]
                sum1 -= color[1]
                sum2 -= color[2]

                color[0] = sum0 / kernelSize
                color[1] = sum1 / kernelSize
                color[2] = sum2 / kernelSize
                target.setPixel(targetX++, y, model, color)
            }
        }
    }

    private fun boxBlurVertical(source: DoubleImage, target: DoubleImage, width: Int, height: Int, boxRadius: Int) {
        val kernelSize = boxRadius + boxRadius + 1
        val samples = DoubleArray(3)

        for (x in 0 until width) {
            source.getPixel(x, 0, model, samples)
            val first0: Double = samples[0]
            val first1: Double = samples[1]
            val first2: Double = samples[2]

            source.getPixel(x, height - 1, model, samples)
            val last0: Double = samples[0]
            val last1: Double = samples[1]
            val last2: Double = samples[2]

            var leftY = 0
            var rightY = boxRadius
            var targetY = 0

            var sum0 = first0 * (boxRadius + 1)
            var sum1 = first1 * (boxRadius + 1)
            var sum2 = first2 * (boxRadius + 1)
            for (y in 0 until boxRadius) {
                source.getPixel(x, y, model, samples)
                sum0 += samples[0]
                sum1 += samples[1]
                sum2 += samples[2]
            }
            for (y in 0..boxRadius) {
                source.getPixel(x, rightY++, model, samples)
                sum0 += samples[0] - first0
                sum1 += samples[1] - first1
                sum2 += samples[2] - first2
                samples[0] = sum0 / kernelSize
                samples[1] = sum1 / kernelSize
                samples[2] = sum2 / kernelSize
                target.setPixel(x, targetY++, model, samples)
            }
            for (y in boxRadius + 1 until height - boxRadius) {
                source.getPixel(x, rightY++, model, samples)
                sum0 += samples[0]
                sum1 += samples[1]
                sum2 += samples[2]

                source.getPixel(x, leftY++, model, samples)
                sum0 -= samples[0]
                sum1 -= samples[1]
                sum2 -= samples[2]

                samples[0] = sum0 / kernelSize
                samples[1] = sum1 / kernelSize
                samples[2] = sum2 / kernelSize
                target.setPixel(x, targetY++, model, samples)
            }
            for (y in height - boxRadius until height) {
                sum0 += last0
                sum1 += last1
                sum2 += last2

                source.getPixel(x, leftY++, model, samples)
                sum0 -= samples[0]
                sum1 -= samples[1]
                sum2 -= samples[2]

                samples[0] = sum0 / kernelSize
                samples[1] = sum1 / kernelSize
                samples[2] = sum2 / kernelSize
                target.setPixel(x, targetY++, model, samples)
            }
        }
    }

    private fun boxSizesForGauss(sigma: Double, n: Int): DoubleArray {
        val wIdeal = Math.sqrt((12 * sigma * sigma / n) + 1)
        var wl = Math.floor(wIdeal)
        if (wl % 2 == 0.0) wl--
        val wu: Double = wl + 2
        val mIdeal: Double = ((12 * sigma * sigma) - (n * wl * wl) - (4 * n * wl) - (3 * n)) / (-4 * wl - 4)
        val m: Long = Math.round(mIdeal)
        val sizes = DoubleArray(n)
        for (i in 0 until n) {
            sizes[i] = if (i < m) wl else wu
        }
        return sizes
    }

    override fun toString(): String {
        return "GaussianBlur(radius=$radius)"
    }
}