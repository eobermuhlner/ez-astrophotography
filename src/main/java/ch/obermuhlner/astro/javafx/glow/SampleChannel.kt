package ch.obermuhlner.astro.javafx.glow

import ch.obermuhlner.astro.image.color.ColorModel

enum class SampleChannel(val colorModel: ColorModel, val sampleIndex: Int) {
    Red(ColorModel.RGB, 0),
    Green(ColorModel.RGB, 1),
    Blue(ColorModel.RGB, 2),
    Hue(ColorModel.HSV, 0),
    Saturation(ColorModel.HSV, 1),
    Brightness(ColorModel.HSV, 2);

}