package ch.obermuhlner.astro.gradient.operation

import ch.obermuhlner.astro.gradient.math.SplineInterpolator
import java.util.*

class SubtractSplineImageOperation constructor(private val factor: Double, private vararg val xyPairs: Double) : AbstractSimpleChannelImageOperation() {
    override fun channelOperation(channel1: Double, channel2: Double, x: Int, y: Int, channelIndex: Int): Double {
        if (channel2 <= 0.0) {
            return channel1
        }
        val xPoints: MutableList<Double> = ArrayList()
        val yPoints: MutableList<Double> = ArrayList()
        xPoints.add(0.0)
        yPoints.add(0.0)
        xPoints.add(channel2)
        yPoints.add(channel2 * factor)
        var i: Int = 0
        while (i < xyPairs.size) {
            xPoints.add(xyPairs[i + 0])
            yPoints.add(xyPairs[i + 1])
            i += 2
        }
        xPoints.add(1.0)
        yPoints.add(1.0)
        val spline: SplineInterpolator = SplineInterpolator.createMonotoneCubicSpline(
                xPoints,
                yPoints
        )
        return spline.interpolate(channel1)
    }

    override fun toString(): String {
        return "SubtractSpine(factor=" + factor + ", xy=" + xyPairs.contentToString() + ")"
    }
}