package ch.obermuhlner.astro.gradient.correction

class LinearSampleSubtraction : SampleSubtraction {
    public override fun subtract(sample: Double, delta: Double): Double {
        return (sample - delta) / (1.0 - delta)
    }
}