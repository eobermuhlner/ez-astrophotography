package ch.obermuhlner.astro.gradient.correction

open interface SampleSubtraction {
    fun subtract(sample: Double, delta: Double): Double

    fun subtract(sample: DoubleArray, delta: DoubleArray, result: DoubleArray?): DoubleArray {
        var result: DoubleArray? = result
        if (result == null) {
            result = DoubleArray(3)
        }
        for (i in 0..2) {
            result[i] = subtract(sample[i], delta[i])
        }
        return result
    }
}