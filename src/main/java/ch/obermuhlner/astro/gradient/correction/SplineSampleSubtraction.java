package ch.obermuhlner.astro.gradient.correction;

import ch.obermuhlner.astro.gradient.math.SplineInterpolator;

import java.util.Arrays;

public class SplineSampleSubtraction implements SampleSubtraction {

  private final double factor;

  public SplineSampleSubtraction(double factor) {
    this.factor = factor;
  }

  @Override
  public double subtract(double sample, double delta) {
    if (delta <= 0.0) {
      return sample;
    }

    SplineInterpolator spline = SplineInterpolator.createMonotoneCubicSpline(
        Arrays.asList(0.0, delta, 1.0),
        Arrays.asList(0.0, delta * factor, 1.0)
    );

    return spline.interpolate(sample);
  }
}
