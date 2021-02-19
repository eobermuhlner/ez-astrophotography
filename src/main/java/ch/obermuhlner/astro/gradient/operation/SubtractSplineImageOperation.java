package ch.obermuhlner.astro.gradient.operation;

import ch.obermuhlner.astro.gradient.math.SplineInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubtractSplineImageOperation extends AbstractSimpleChannelImageOperation {

  private final double factor;
  private final double[] xyPairs;

  public SubtractSplineImageOperation(double factor, double... xyPairs) {
    this.factor = factor;
    this.xyPairs = xyPairs;
  }

  @Override
  protected double channelOperation(double channel1, double channel2, int x, int y, int channelIndex) {
    if (channel2 <= 0.0) {
      return channel1;
    }

    List<Double> xPoints = new ArrayList<>();
    List<Double> yPoints = new ArrayList<>();

    xPoints.add(0.0);
    yPoints.add(0.0);

    xPoints.add(channel2);
    yPoints.add(channel2 * factor);

    for (int i = 0; i < xyPairs.length; i+=2) {
      xPoints.add(xyPairs[i + 0]);
      yPoints.add(xyPairs[i + 1]);
    }

    xPoints.add(1.0);
    yPoints.add(1.0);

    SplineInterpolator spline = SplineInterpolator.createMonotoneCubicSpline(
        xPoints,
        yPoints
    );

    return spline.interpolate(channel1);
  }

  @Override
  public String toString() {
    return "SubtractSpine(factor=" + factor + ", xy=" + Arrays.toString(xyPairs) + ")";
  }
}
