package ch.obermuhlner.astro.gradient.operation

class SimpleChannelImageOperation constructor(private val channelOperation: ChannelOperation) : AbstractSimpleChannelImageOperation() {
    override fun channelOperation(channel1: Double, channel2: Double, x: Int, y: Int, channelIndex: Int): Double {
        return channelOperation.operation(channel1, channel2, x, y, channelIndex)
    }

    open interface ChannelOperation {
        fun operation(channel1: Double, channel2: Double, x: Int, y: Int, channelIndex: Int): Double
    }
}