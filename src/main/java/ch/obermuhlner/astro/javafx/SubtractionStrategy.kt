package ch.obermuhlner.astro.javafx

import ch.obermuhlner.astro.gradient.operation.ImageOperation
import ch.obermuhlner.astro.gradient.operation.SubtractImageOperation
import ch.obermuhlner.astro.gradient.operation.SubtractLinearImageOperation
import ch.obermuhlner.astro.gradient.operation.SubtractSplineImageOperation

enum class SubtractionStrategy(private val text: String, val operation: ImageOperation) {
    Subtract("Subtract", SubtractImageOperation()),
    SubtractLinear("Subtract Linear", SubtractLinearImageOperation()),
    Spline_1("Spline 1%", SubtractSplineImageOperation(0.01)),
    Spline_1_Stretch("Spline 1% + Stretch", SubtractSplineImageOperation(0.01, 0.7, 0.9)),
    Spline_10("Spline 10%", SubtractSplineImageOperation(0.1));

    public override fun toString(): String {
        return text
    }
}