package ch.obermuhlner.astro.gradient.filter

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.color.ColorModel

class BoxBlurFilter constructor(private val radius: Int, private val model: ColorModel) : AbstractFilter() {
    private val kernelSize: Int = (radius + radius + 1) * (radius + radius + 1)

    override fun filterPixel(source: DoubleImage, x: Int, y: Int, colorModel: ColorModel, color: DoubleArray): DoubleArray {
        var sum0 = 0.0
        var sum1 = 0.0
        var sum2 = 0.0
        for (kx in x - radius until x + radius) {
            for (ky in y - radius until y + radius) {
                source.getPixel(kx, ky, model, color)
                sum0 += color[0]
                sum1 += color[1]
                sum2 += color[2]
            }
        }
        color[0] = sum0 / kernelSize
        color[1] = sum1 / kernelSize
        color[2] = sum2 / kernelSize
        return color
    }

    override fun toString(): String {
        return "BoxBlur(radius=$radius)"
    }

}