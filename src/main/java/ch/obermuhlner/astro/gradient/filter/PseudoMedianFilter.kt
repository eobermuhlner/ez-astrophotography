package ch.obermuhlner.astro.gradient.filter

import ch.obermuhlner.astro.image.ArrayDoubleImage
import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel

class PseudoMedianFilter constructor(private val radius: Int, private val model: ColorModel) : Filter {
    private val horizontalMedianFilter: HorizontalMedianFilter = HorizontalMedianFilter(radius, model)
    private val verticalMedianFilter: VerticalMedianFilter = VerticalMedianFilter(radius, model)

    override fun filter(source: DoubleImage, target: DoubleImage, width: Int, height: Int): DoubleImage {
        val temp = ArrayDoubleImage(width, height, target.colorModel)
        horizontalMedianFilter.filter(source, temp, width, height)
        verticalMedianFilter.filter(temp, target, width, height)
        return target
    }

    override fun toString(): String {
        return "PseudoMedianFilter(radius=$radius)"
    }

}