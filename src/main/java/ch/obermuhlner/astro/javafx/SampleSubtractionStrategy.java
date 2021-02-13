package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.gradient.correction.LinearSampleSubtraction;
import ch.obermuhlner.astro.gradient.correction.SampleSubtraction;
import ch.obermuhlner.astro.gradient.correction.SimpleSampleSubtraction;
import ch.obermuhlner.astro.gradient.correction.SplineSampleSubtraction;

public enum SampleSubtractionStrategy {
  Simple("Simple", new SimpleSampleSubtraction()),
  Linear("Linear", new LinearSampleSubtraction()),
  Spline_1("Spline 1%", new SplineSampleSubtraction(0.01)),
  Spline_10("Spline 10%", new SplineSampleSubtraction(0.1));

  private final String text;
  private final SampleSubtraction sampleSubtraction;

  SampleSubtractionStrategy(String text, SampleSubtraction sampleSubtraction) {
    this.text = text;
    this.sampleSubtraction = sampleSubtraction;
  }

  public SampleSubtraction getSampleSubtraction() {
    return sampleSubtraction;
  }

  @Override
  public String toString() {
    return text;
  }
}
