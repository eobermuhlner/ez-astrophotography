package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.ArrayDoubleImage;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.color.ColorModel;

public class PseudoMedianFilter implements Filter {

  private final int radius;
  private final ColorModel model;

  private final HorizontalMedianFilter horizontalMedianFilter;
  private final VerticalMedianFilter verticalMedianFilter;

  public PseudoMedianFilter(int radius, ColorModel model) {
    this.radius = radius;
    this.model = model;
    horizontalMedianFilter = new HorizontalMedianFilter(radius, model);
    verticalMedianFilter = new VerticalMedianFilter(radius, model);
  }

  @Override
  public DoubleImage filter(DoubleImage source, DoubleImage target, int width, int height) {
    ArrayDoubleImage temp = new ArrayDoubleImage(width, height, target.getColorModel());
    horizontalMedianFilter.filter(source, temp, width, height);
    verticalMedianFilter.filter(temp, target, width, height);
    return target;
  }

  @Override
  public String toString() {
    return "PseudoMedianFilter(radius=" + radius + ")";
  }
}
