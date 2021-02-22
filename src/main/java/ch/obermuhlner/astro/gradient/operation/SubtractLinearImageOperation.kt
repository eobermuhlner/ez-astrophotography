package ch.obermuhlner.astro.gradient.operation

class SubtractLinearImageOperation : AbstractSimpleChannelImageOperation() {
    override fun channelOperation(channel1: Double, channel2: Double, x: Int, y: Int, channelIndex: Int): Double {
        return (channel1 - channel2) / (1.0 - channel2)
    }

    override fun toString(): String {
        return "SubtractLinear"
    }
}