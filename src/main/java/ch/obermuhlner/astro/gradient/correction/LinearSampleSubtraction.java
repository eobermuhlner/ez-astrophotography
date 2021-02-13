package ch.obermuhlner.astro.gradient.correction;

public class LinearSampleSubtraction implements SampleSubtraction {

  @Override
  public double subtract(double sample, double delta) {
    return (sample - delta) / (1.0 - delta);
  }
}
