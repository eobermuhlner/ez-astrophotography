package ch.obermuhlner.astro.gradient.correction;

import ch.obermuhlner.astro.gradient.math.SplineInterpolator;

import java.util.ArrayList;
import java.util.List;

public class SplineSampleSubtraction implements SampleSubtraction {

  private final double factor;
  private final double[] xyPairs;

  public SplineSampleSubtraction(double factor, double... xyPairs) {
    this.factor = factor;
    this.xyPairs = xyPairs;
  }

  @Override
  public double subtract(double sample, double delta) {
    if (delta <= 0.0) {
      return sample;
    }

    List<Double> xPoints = new ArrayList<>();
    List<Double> yPoints = new ArrayList<>();

    xPoints.add(0.0);
    yPoints.add(0.0);

    xPoints.add(delta);
    yPoints.add(delta * factor);

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

    return spline.interpolate(sample);
  }
}
