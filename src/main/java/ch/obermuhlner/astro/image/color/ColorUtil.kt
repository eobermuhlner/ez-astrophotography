package ch.obermuhlner.astro.image.color

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object ColorUtil {
    fun toIntRGB(rgb: DoubleArray): Int {
        val rr = max(min((rgb[ColorModel.RGB.R] * 256).toInt(), 255), 0)
        val gg = max(min((rgb[ColorModel.RGB.G] * 256).toInt(), 255), 0)
        val bb = max(min((rgb[ColorModel.RGB.B] * 256).toInt(), 255), 0)
        return (rr * 0x100 + gg) * 0x100 + bb
    }

    fun toIntARGB(rgb: DoubleArray): Int {
        return -0x1000000 or toIntRGB(rgb)
    }

    @JvmOverloads
    fun convertRGBtoHSV(rgb: DoubleArray, hsv: DoubleArray, rgbOffset: Int = 0, hsvOffset: Int = 0): DoubleArray {
        return convertRGBtoHSV(rgb[rgbOffset + ColorModel.RGB.R], rgb[rgbOffset + ColorModel.RGB.G], rgb[rgbOffset + ColorModel.RGB.B], hsv, hsvOffset)
    }

    @JvmOverloads
    fun convertRGBtoHSV(r: Double, g: Double, b: Double, hsv: DoubleArray?, hsvOffset: Int = 0): DoubleArray {
        var hsv = hsv
        var h: Double
        val s: Double
        val v: Double
        var cmax = if (r > g) r else g
        if (b > cmax) cmax = b
        var cmin = if (r < g) r else g
        if (b < cmin) cmin = b
        v = cmax
        s = if (cmax != 0.0) (cmax - cmin) / cmax else 0.0
        if (s == 0.0) {
            h = 0.0
        } else {
            val redc = (cmax - r) / (cmax - cmin)
            val greenc = (cmax - g) / (cmax - cmin)
            val bluec = (cmax - b) / (cmax - cmin)
            h = if (r == cmax) bluec - greenc else if (g == cmax) 2.0 + redc - bluec else 4.0 + greenc - redc
            h = h / 6.0
            if (h < 0) h = h + 1.0
        }
        if (hsv == null) {
            hsv = DoubleArray(3)
        }
        hsv[hsvOffset + ColorModel.HSV.H] = h * 360
        hsv[hsvOffset + ColorModel.HSV.S] = s
        hsv[hsvOffset + ColorModel.HSV.V] = v
        return hsv
    }

    @JvmOverloads
    fun convertHSVtoRGB(hsv: DoubleArray, rgb: DoubleArray, hsvOffset: Int = 0, rgbOffset: Int = 0): DoubleArray {
        return convertHSVtoRGB(hsv[hsvOffset + ColorModel.HSV.H], hsv[hsvOffset + ColorModel.HSV.S], hsv[hsvOffset + ColorModel.HSV.V], rgb, rgbOffset)
    }

    @JvmOverloads
    fun convertHSVtoRGB(h: Double, s: Double, v: Double, rgb: DoubleArray?, rgbOffset: Int = 0): DoubleArray {
        var h = h
        var rgb = rgb
        val normalizedHue = (h % 360 + 360) % 360
        h = normalizedHue / 360
        var r = 0.0
        var g = 0.0
        var b = 0.0
        if (s == 0.0) {
            b = v
            g = b
            r = g
        } else {
            val hh = (h - floor(h)) * 6.0
            val f = hh - floor(hh)
            val p = v * (1.0 - s)
            val q = v * (1.0 - s * f)
            val t = v * (1.0 - s * (1.0 - f))
            when (hh.toInt()) {
                0 -> {
                    r = v
                    g = t
                    b = p
                }
                1 -> {
                    r = q
                    g = v
                    b = p
                }
                2 -> {
                    r = p
                    g = v
                    b = t
                }
                3 -> {
                    r = p
                    g = q
                    b = v
                }
                4 -> {
                    r = t
                    g = p
                    b = v
                }
                5 -> {
                    r = v
                    g = p
                    b = q
                }
            }
        }
        if (rgb == null) {
            rgb = DoubleArray(3)
        }
        rgb[rgbOffset + ColorModel.RGB.R] = r
        rgb[rgbOffset + ColorModel.RGB.G] = g
        rgb[rgbOffset + ColorModel.RGB.B] = b
        return rgb
    }

    fun sampleDistance(deltaSample: DoubleArray, colorModel: ColorModel, sampleIndex: Int, normalize: Boolean): Double {
        var delta = deltaSample[sampleIndex]
        if (colorModel === ColorModel.HSV && sampleIndex == ColorModel.HSV.H) {
            if (delta > 180) {
                delta -= 180.0
            } else if (delta < -180) {
                delta += 180.0
            }
            if (normalize) {
                delta = delta / 180
            }
        }
        return delta
    }

    fun convert(sourceModel: ColorModel, source: DoubleArray, targetModel: ColorModel, target: DoubleArray = DoubleArray(3)): DoubleArray {
        if (sourceModel === ColorModel.RGB) {
            if (targetModel === ColorModel.HSV) {
                convertRGBtoHSV(source, target)
            }
        } else if (sourceModel === ColorModel.HSV) {
            if (targetModel === ColorModel.RGB) {
                convertHSVtoRGB(source, target)
            }
        }
        return target
    }

    fun convertRGBtoLuminosity(rgb: DoubleArray): Double {
        return 0.2126 * rgb[ColorModel.RGB.R] + 0.7152 * rgb[ColorModel.RGB.G] + 0.0722 * rgb[ColorModel.RGB.G]
    }
}