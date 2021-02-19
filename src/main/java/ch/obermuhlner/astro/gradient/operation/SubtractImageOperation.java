package ch.obermuhlner.astro.gradient.operation;

public class SubtractImageOperation extends AbstractSimpleChannelImageOperation {

  @Override
  protected double channelOperation(double channel1, double channel2, int x, int y, int channelIndex) {
    return channel1 - channel2;
  }

  @Override
  public String toString() {
    return "Subtract";
  }
}
