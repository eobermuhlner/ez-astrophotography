package ch.obermuhlner.astro.gradient.correction;

public class SimpleSampleSubtraction implements SampleSubtraction {

  @Override
  public double subtract(double sample, double delta) {
    return sample - delta;
  }
}
