package ch.obermuhlner.astro.gradient.operation;

public class SimpleChannelImageOperation extends AbstractSimpleChannelImageOperation {

  private final ChannelOperation channelOperation;

  public SimpleChannelImageOperation(ChannelOperation channelOperation) {
    this.channelOperation = channelOperation;
  }

  @Override
  protected double channelOperation(double channel1, double channel2, int x, int y, int channelIndex) {
    return channelOperation.operation(channel1, channel2, x, y, channelIndex);
  }

  public interface ChannelOperation {
    double operation(double channel1, double channel2, int x, int y, int channelIndex);
  }
}
