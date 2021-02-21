package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.gradient.operation.ImageOperation;
import ch.obermuhlner.astro.gradient.operation.SubtractImageOperation;
import ch.obermuhlner.astro.gradient.operation.SubtractLinearImageOperation;
import ch.obermuhlner.astro.gradient.operation.SubtractSplineImageOperation;

public enum SubtractionStrategy {
  Subtract("Subtract", new SubtractImageOperation()),
  SubtractLinear("Subtract Linear", new SubtractLinearImageOperation()),
  Spline_1("Spline 1%", new SubtractSplineImageOperation(0.01)),
  Spline_1_Stretch("Spline 1% + Stretch", new SubtractSplineImageOperation(0.01, 0.7, 0.9)),
  Spline_10("Spline 10%", new SubtractSplineImageOperation(0.1));

  private final String text;
  private final ImageOperation operation;

  SubtractionStrategy(String text, ImageOperation operation) {
    this.text = text;
    this.operation = operation;
  }

  public ImageOperation getOperation() {
    return operation;
  }

  @Override
  public String toString() {
    return text;
  }
}
