package ch.obermuhlner.astro.gradient.correction

import ch.obermuhlner.astro.gradient.math.SplineInterpolator
import java.util.*

class SplineSampleSubtraction constructor(private val factor: Double, private vararg val xyPairs: Double) : SampleSubtraction {
    override fun subtract(sample: Double, delta: Double): Double {
        if (delta <= 0.0) {
            return sample
        }
        val xPoints: MutableList<Double> = ArrayList()
        val yPoints: MutableList<Double> = ArrayList()
        xPoints.add(0.0)
        yPoints.add(0.0)
        xPoints.add(delta)
        yPoints.add(delta * factor)
        var i = 0
        while (i < xyPairs.size) {
            xPoints.add(xyPairs.get(i + 0))
            yPoints.add(xyPairs.get(i + 1))
            i += 2
        }
        xPoints.add(1.0)
        yPoints.add(1.0)
        val spline: SplineInterpolator = SplineInterpolator.Companion.createMonotoneCubicSpline(
                xPoints,
                yPoints
        )
        return spline.interpolate(sample)
    }
}