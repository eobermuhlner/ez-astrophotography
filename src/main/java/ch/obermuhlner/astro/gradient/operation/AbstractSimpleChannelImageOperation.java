package ch.obermuhlner.astro.gradient.operation;

public abstract class AbstractSimpleChannelImageOperation extends AbstractSimplePixelImageOperation {

  @Override
  protected double[] pixelOperation(double[] pixel1, double[] pixel2, int x, int y, double[] result) {
    result[0] = channelOperation(pixel1[0], pixel2[0], x, y, 0);
    result[1] = channelOperation(pixel1[1], pixel2[1], x, y, 1);
    result[2] = channelOperation(pixel1[2], pixel2[2], x, y, 2);

    return result;
  }

  protected abstract double channelOperation(double channel1, double channel2, int x, int y, int channelIndex);
}