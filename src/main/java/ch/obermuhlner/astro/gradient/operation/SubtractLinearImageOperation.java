package ch.obermuhlner.astro.gradient.operation;

public class SubtractLinearImageOperation extends AbstractSimpleChannelImageOperation {

  @Override
  protected double channelOperation(double channel1, double channel2, int x, int y, int channelIndex) {
    return (channel1 - channel2) / (1.0 - channel2);
  }

  @Override
  public String toString() {
    return "SubtractLinear";
  }
}
