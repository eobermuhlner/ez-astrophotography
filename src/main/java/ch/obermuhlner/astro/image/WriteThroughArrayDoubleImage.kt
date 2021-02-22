package ch.obermuhlner.astro.image

import ch.obermuhlner.astro.image.color.ColorModel

class WriteThroughArrayDoubleImage(private val image: DoubleImage, colorModel: ColorModel) : DoubleImage {
    private val arrayImage: ArrayDoubleImage = ArrayDoubleImage(image.width, image.height, colorModel)

    override val width: Int
        get() = arrayImage.width
    override val height: Int
        get() = arrayImage.height

    override fun getNativePixel(x: Int, y: Int, color: DoubleArray?): DoubleArray {
        return arrayImage.getNativePixel(x, y, color)
    }

    override fun setNativePixel(x: Int, y: Int, color: DoubleArray) {
        arrayImage.setNativePixel(x, y, color)
        image.setNativePixel(x, y, color)
    }

}