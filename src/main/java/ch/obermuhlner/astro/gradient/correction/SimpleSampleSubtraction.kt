package ch.obermuhlner.astro.gradient.correction

class SimpleSampleSubtraction : SampleSubtraction {
    override fun subtract(sample: Double, delta: Double): Double {
        return sample - delta
    }
}