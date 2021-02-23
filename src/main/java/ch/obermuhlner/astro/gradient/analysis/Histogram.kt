package ch.obermuhlner.astro.gradient.analysis

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import kotlin.math.max
import kotlin.math.min

class Histogram constructor(val colorModel: ColorModel, val binCount: Int) {
    private val channelBins: Array<IntArray> = Array(3) { IntArray(binCount) }
    private var maxSampleCountInBin: Int = 0

    operator fun get(channelIndex: Int, binIndex: Int): Double {
        return (channelBins[channelIndex][binIndex].toDouble()) / maxSampleCountInBin
    }

    private fun getChannelBins(sampleIndex: Int, bins: DoubleArray = DoubleArray(binCount)): DoubleArray {
        for (i in 0 until binCount) {
            bins[i] = (channelBins[sampleIndex][i].toDouble()) / maxSampleCountInBin
        }
        return bins
    }

    @JvmOverloads
    fun sampleImage(image: DoubleImage, x: Int = 0, y: Int = 0, width: Int = image.width, height: Int = image.height) {
        val pixel = DoubleArray(3)
        clear()
        for (iy in 0 until height) {
            for (ix in 0 until width) {
                if (image.isInside(x + ix, y + iy)) {
                    image.getPixel(x + ix, y + iy, colorModel, pixel)
                    addSample(pixel, 0)
                    addSample(pixel, 1)
                    addSample(pixel, 2)
                }
            }
        }
        maxSampleCountInBin = 0
        for (sampleIndex in channelBins.indices) {
            for (binIndex in 0 until binCount) {
                maxSampleCountInBin = max(maxSampleCountInBin, channelBins[sampleIndex][binIndex])
            }
        }
    }

    private fun clear() {
        for (sampleIndex in channelBins.indices) {
            for (binIndex in 0 until binCount) {
                channelBins[sampleIndex][binIndex] = 0
            }
        }
    }

    private fun addSample(pixel: DoubleArray, sampleIndex: Int) {
        var value: Double = pixel[sampleIndex]
        if (colorModel === ColorModel.HSV && sampleIndex == ColorModel.HSV.H) {
            value /= 360.0
        }
        val binIndex = max(0, min(binCount - 1, (value * binCount).toInt()))
        channelBins[sampleIndex][binIndex]++
    }
}