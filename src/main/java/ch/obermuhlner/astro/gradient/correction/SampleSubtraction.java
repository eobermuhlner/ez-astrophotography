package ch.obermuhlner.astro.gradient.correction;

public interface SampleSubtraction {
  double subtract(double sample, double delta);

  default double[] subtract(double[] sample, double[] delta, double[] result) {
    if (result == null) {
      result = new double[3];
    }
    for (int i = 0; i < 3; i++) {
      result[i] = subtract(sample[i], delta[i]);
    }
    return result;
  }
}
