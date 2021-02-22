package ch.obermuhlner.astro.gradient.correction

class LinearSampleSubtraction : SampleSubtraction {
    override fun subtract(sample: Double, delta: Double): Double {
        return (sample - delta) / (1.0 - delta)
    }
}