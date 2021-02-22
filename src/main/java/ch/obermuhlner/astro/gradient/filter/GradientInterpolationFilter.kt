package ch.obermuhlner.astro.gradient.filter

import ch.obermuhlner.astro.gradient.Point
import ch.obermuhlner.astro.gradient.points.AllPointsFinder
import ch.obermuhlner.astro.gradient.points.PointsFinder
import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorModel.RGB
import java.util.*
import kotlin.math.pow

class GradientInterpolationFilter : Filter {
    private var pointsFinder: PointsFinder = AllPointsFinder()
    var interpolationPower: Double = 3.0
    private val adaptiveGradient: Boolean = false
    private val mapPointToColor: MutableMap<Point, DoubleArray> = HashMap()

    constructor() {}

    constructor(interpolationPower: Double) {
        this.interpolationPower = interpolationPower
    }

    fun setPointsFinder(pointsFinder: PointsFinder) {
        this.pointsFinder = pointsFinder
        pointsFinder.setFixPoints(mapPointToColor.keys)
    }

    fun setFixPoints(fixPoints: List<Point>, image: DoubleImage, sampleRadius: Int) {
        val fixColors: MutableList<DoubleArray> = ArrayList()
        for (fixPoint: Point in fixPoints) {
            fixColors.add(image.averagePixel(fixPoint.x - sampleRadius,
                    fixPoint.y - sampleRadius,
                    sampleRadius + sampleRadius + 1,
                    sampleRadius + sampleRadius + 1))
        }
        setFixPoints(fixPoints, fixColors)
    }

    fun setFixPoints(fixPoints: List<Point>, fixColors: List<DoubleArray>) {
        mapPointToColor.clear()
        for (i in fixPoints.indices) {
            mapPointToColor.put(fixPoints[i], fixColors[i])
        }
        pointsFinder.setFixPoints(fixPoints)
    }

    private fun getRelevantFixPoints(point: Point): List<Point> {
        return pointsFinder.getRelevantFixPoints(point)
    }

    private fun getRelevantFixColors(relevantFixPoints: List<Point>): List<DoubleArray> {
        val result: MutableList<DoubleArray> = ArrayList()
        for (relevantFixPoint: Point in relevantFixPoints) {
            result.add(mapPointToColor[relevantFixPoint]!!)
        }
        return result
    }

    override fun filter(source: DoubleImage, target: DoubleImage, width: Int, height: Int): DoubleImage {
        val sourceColor = DoubleArray(3)
        val gradientColor = DoubleArray(3)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val point = Point(x, y)
                val relevantFixPoints = getRelevantFixPoints(point)
                val relevantFixColors = getRelevantFixColors(relevantFixPoints)
                val n = relevantFixPoints.size
                val distances = DoubleArray(n)
                val factors = DoubleArray(n)
                var totalDistance = 0.0
                for (i in 0 until n) {
                    val gradientPoint = relevantFixPoints[i]
                    distances[i] = point.distance(gradientPoint)
                    totalDistance += distances[i]
                }
                var totalFactor = 0.0
                for (i in 0 until n) {
                    var factor = 1.0 - distances[i] / totalDistance
                    factor = factor.pow(interpolationPower)
                    factors[i] = factor
                    totalFactor += factor
                }
                if (n == 1) {
                    factors[0] = 1.0
                    totalFactor = 1.0
                }
                gradientColor[ColorModel.RGB.R] = 0.0
                gradientColor[ColorModel.RGB.G] = 0.0
                gradientColor[ColorModel.RGB.B] = 0.0
                for (i in 0 until n) {
                    val factor: Double = factors[i] / totalFactor
                    val fixColor: DoubleArray = relevantFixColors[i]
                    gradientColor[ColorModel.RGB.R] += fixColor[ColorModel.RGB.R] * factor
                    gradientColor[ColorModel.RGB.G] += fixColor[ColorModel.RGB.G] * factor
                    gradientColor[ColorModel.RGB.B] += fixColor[ColorModel.RGB.B] * factor
                }
                source.getPixel(x, y, ColorModel.RGB, sourceColor)
                if (adaptiveGradient) {
//          HSVColor imageHSV = HSVColor.fromRGB(inputColor);
//          HSVColor gradientHSV = HSVColor.fromRGB(gradientColor);
//          double v = (imageHSV.v + gradientHSV.v) / 2;
//          gradientHSV = new HSVColor(gradientHSV.h, gradientHSV.s, v);
//          gradientColor = RGBColor.fromHSV(gradientHSV);
                }
                target.setPixel(x, y, ColorModel.RGB, gradientColor)
            }
        }
        return target
    }

    public override fun toString(): String {
        return "Gradient{pointsFinder=$pointsFinder, interpolationPower=$interpolationPower, adaptiveGradient=$adaptiveGradient, mapPointToColor=$mapPointToColor}"
    }
}