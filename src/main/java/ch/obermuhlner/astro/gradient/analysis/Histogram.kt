package ch.obermuhlner.astro.gradient.analysis

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel

class Histogram constructor(val colorModel: ColorModel, val binCount: Int) {
    private var sampleCount: Int = 0
    private val sampleBins: Array<IntArray?>
    private var maxSampleCountInBin: Int = 0
    fun getBin(sampleIndex: Int, binIndex: Int): Double {
        return (sampleBins.get(sampleIndex)!!.get(binIndex).toDouble()) / maxSampleCountInBin
    }

    fun getRawSampleBins(sampleIndex: Int): IntArray? {
        return sampleBins.get(sampleIndex)
    }

    fun getSampleBins(sampleIndex: Int): DoubleArray {
        return getSampleBins(sampleIndex, null)
    }

    fun getSampleBins(sampleIndex: Int, bins: DoubleArray?): DoubleArray {
        var bins: DoubleArray? = bins
        if (bins == null) {
            bins = DoubleArray(binCount)
        }
        for (i in 0 until binCount) {
            bins[i] = (sampleBins.get(sampleIndex)!![i].toDouble()) / maxSampleCountInBin
        }
        return bins
    }

    @JvmOverloads
    fun sampleImage(image: DoubleImage, x: Int = 0, y: Int = 0, width: Int = image.width, height: Int = image.height) {
        val pixel: DoubleArray = DoubleArray(3)
        clear()
        for (iy in 0 until height) {
            for (ix in 0 until width) {
                if (image.isInside(x + ix, y + iy)) {
                    image.getPixel(x + ix, y + iy, colorModel, pixel)
                    addSample(pixel, 0)
                    addSample(pixel, 1)
                    addSample(pixel, 2)
                    sampleCount++
                }
            }
        }
        maxSampleCountInBin = 0
        for (sampleIndex in sampleBins.indices) {
            for (binIndex in 0 until binCount) {
                maxSampleCountInBin = Math.max(maxSampleCountInBin, sampleBins.get(sampleIndex)!!.get(binIndex))
            }
        }
    }

    fun clear() {
        sampleCount = 0
        for (sampleIndex in sampleBins.indices) {
            for (binIndex in 0 until binCount) {
                sampleBins.get(sampleIndex)!![binIndex] = 0
            }
        }
    }

    private fun addSample(pixel: DoubleArray, sampleIndex: Int) {
        var value: Double = pixel.get(sampleIndex)
        if (colorModel === ColorModel.HSV && sampleIndex == ColorModel.HSV.H) {
            value = value / 360.0
        }
        var binIndex: Int = (value * binCount).toInt()
        binIndex = Math.max(0, binIndex)
        binIndex = Math.min(binCount - 1, binIndex)
        sampleBins.get(sampleIndex)!![binIndex]++
    }

    init {
        sampleBins = arrayOfNulls(3)
        for (i in 0..2) {
            sampleBins[i] = IntArray(binCount)
        }
    }
}