package ch.obermuhlner.astro.gradient.operation

abstract class AbstractSimpleChannelImageOperation : AbstractSimplePixelImageOperation() {
    override fun pixelOperation(pixel1: DoubleArray, pixel2: DoubleArray, x: Int, y: Int, result: DoubleArray): DoubleArray {
        result[0] = channelOperation(pixel1[0], pixel2[0], x, y, 0)
        result[1] = channelOperation(pixel1[1], pixel2[1], x, y, 1)
        result[2] = channelOperation(pixel1[2], pixel2[2], x, y, 2)
        return result
    }

    protected abstract fun channelOperation(channel1: Double, channel2: Double, x: Int, y: Int, channelIndex: Int): Double
}