package ch.obermuhlner.astro.image

import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorUtil
import java.util.*
import kotlin.math.max
import kotlin.math.min

interface DoubleImage {
    val width: Int
    val height: Int
    val colorModel: ColorModel
        get() = ColorModel.RGB

    fun getNativePixel(x: Int, y: Int, color: DoubleArray = DoubleArray(3)): DoubleArray
    fun setNativePixel(x: Int, y: Int, color: DoubleArray)

    operator fun get(x: Int, y: Int) = getPixel(x, y)
    operator fun set(x: Int, y: Int, color: DoubleArray) = setPixel(x, y, color)

    fun getPixel(x: Int, y: Int, colorModel: ColorModel = ColorModel.RGB, color: DoubleArray = DoubleArray(3)): DoubleArray {
        val xx = max(0, min(width - 1, x))
        val yy = max(0, min(height - 1, y))
        val result = getNativePixel(xx, yy, color)
        if (colorModel !== this.colorModel) {
            ColorUtil.convert(colorModel, result, this.colorModel, result)
        }
        return result
    }

    fun setPixel(x: Int, y: Int, color: DoubleArray) {
        setPixel(x, y, ColorModel.RGB, color)
    }

    fun setPixel(x: Int, y: Int, colorModel: ColorModel, color: DoubleArray) {
        if (isInside(x, y)) {
            var nativeColor = color
            if (colorModel !== this.colorModel) {
                nativeColor = ColorUtil.convert(colorModel, nativeColor, this.colorModel)
            }
            setNativePixel(x, y, nativeColor)
        }
    }

    fun setPixels(fillColor: DoubleArray) {
        setPixels(ColorModel.RGB, fillColor)
    }

    fun setPixels(colorModel: ColorModel, fillColor: DoubleArray) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                setPixel(x, y, colorModel, fillColor)
            }
        }
    }

    fun setPixels(x: Int, y: Int, width: Int, height: Int, colorModel: ColorModel = ColorModel.RGB, fillColor: DoubleArray) {
        croppedImage(x, y, width, height).setPixels(colorModel, fillColor)
    }

    fun setPixels(sourceX: Int, sourceY: Int, source: DoubleImage, targetX: Int, targetY: Int, width: Int, height: Int, colorModel: ColorModel = ColorModel.RGB, outsideColor: DoubleArray? = null) {
        croppedImage(targetX, targetY, width, height).setPixels(source.croppedImage(sourceX, sourceY, width, height), colorModel, outsideColor)
    }

    fun setPixels(source: DoubleImage, colorModel: ColorModel = ColorModel.RGB, outsideColor: DoubleArray? = null) {
        val color = DoubleArray(3)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (outsideColor == null || source.isInside(x, y)) {
                    source.getPixel(x, y, colorModel, color)
                } else {
                    color[0] = outsideColor[0]
                    color[1] = outsideColor[1]
                    color[2] = outsideColor[2]
                }
                setPixel(x, y, colorModel, color)
            }
        }
    }

    fun isInside(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 && x < width && y < height
    }

    fun isInsideUnderlying(x: Int, y: Int): Boolean {
        return true
    }

    fun isValidPixel(x: Int, y: Int): Boolean {
        return isInside(x, y) && isInsideUnderlying(x, y)
    }

    fun croppedImage(x: Int, y: Int, width: Int, height: Int, strictClipping: Boolean = true): DoubleImage {
        return CroppedDoubleImage(this, x, y, width, height, strictClipping)
    }

    fun averagePixel(x: Int, y: Int, width: Int, height: Int, colorModel: ColorModel = ColorModel.RGB, color: DoubleArray = DoubleArray(3)): DoubleArray {
        return croppedImage(x, y, width, height).averagePixel(colorModel, color)
    }

    fun averagePixel(colorModel: ColorModel = ColorModel.RGB, color: DoubleArray = DoubleArray(3)): DoubleArray {
        var n = 0
        var sample0 = 0.0
        var sample1 = 0.0
        var sample2 = 0.0
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (isValidPixel(x, y)) {
                    getPixel(x, y, colorModel, color)
                    sample0 += color[0]
                    sample1 += color[1]
                    sample2 += color[2]
                    n++
                }
            }
        }
        color[0] = sample0 / n
        color[1] = sample1 / n
        color[2] = sample2 / n
        return color
    }

    fun medianPixel(x: Int, y: Int, width: Int, height: Int, colorModel: ColorModel = ColorModel.RGB, color: DoubleArray = DoubleArray(3)): DoubleArray {
        return croppedImage(x, y, width, height).medianPixel(colorModel, color)
    }

    fun medianPixel(colorModel: ColorModel = ColorModel.RGB, color: DoubleArray = DoubleArray(3)): DoubleArray {
        val data: MutableList<DoubleArray> = ArrayList(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (isValidPixel(x, y)) {
                    val sample = getPixel(x, y, ColorModel.HSV)
                    data.add(sample)
                }
            }
        }
        Collections.sort(data,
                Comparator.comparingDouble { c: DoubleArray -> c[ColorModel.HSV.V] }
                .thenComparing { c: DoubleArray -> c[ColorModel.HSV.S] }
                .thenComparing { c: DoubleArray -> c[ColorModel.HSV.H] })
        val n = data.size
        val nHalf = n / 2
        val nHalfPlus1 = nHalf + 1
        if (n % 2 == 0) {
            color[0] = (data[nHalf][ColorModel.HSV.H] + data[nHalfPlus1][ColorModel.HSV.H]) / 2
            color[1] = (data[nHalf][ColorModel.HSV.S] + data[nHalfPlus1][ColorModel.HSV.S]) / 2
            color[2] = (data[nHalf][ColorModel.HSV.V] + data[nHalfPlus1][ColorModel.HSV.V]) / 2
        } else {
            color[0] = data[nHalf][ColorModel.HSV.H]
            color[1] = data[nHalf][ColorModel.HSV.S]
            color[2] = data[nHalf][ColorModel.HSV.V]
        }
        ColorUtil.convert(ColorModel.HSV, color, colorModel, color)
        return color
    }

    fun darkestPixel(colorModel: ColorModel = ColorModel.RGB, color: DoubleArray = DoubleArray(3)): DoubleArray {
        val tempHSV = DoubleArray(3)
        var bestV = 1.0
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (isValidPixel(x, y)) {
                    getPixel(x, y, ColorModel.HSV, tempHSV)
                    val v = tempHSV[ColorModel.HSV.V]
                    if (v < bestV) {
                        bestV = v
                        getPixel(x, y, colorModel, color)
                    }
                }
            }
        }
        return color
    }

    fun brightestPixel(colorModel: ColorModel = ColorModel.RGB, color: DoubleArray = DoubleArray(3)): DoubleArray {
        val tempHSV = DoubleArray(3)
        var bestV = 0.0
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (isValidPixel(x, y)) {
                    getPixel(x, y, ColorModel.HSV, tempHSV)
                    val v = tempHSV[ColorModel.HSV.V]
                    if (v > bestV) {
                        bestV = v
                        getPixel(x, y, colorModel, color)
                    }
                }
            }
        }
        return color
    }

    fun copyImage(): DoubleImage {
        val copy = ArrayDoubleImage(width, height, colorModel)
        copy.setPixels(this, colorModel, null)
        return copy
    }
}