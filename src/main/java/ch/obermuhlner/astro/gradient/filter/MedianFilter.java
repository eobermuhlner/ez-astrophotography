package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.color.ColorModel;

public class MedianFilter extends AbstractFilter {

  private final int radius;
  private final ColorModel model;

  public MedianFilter(int radius, ColorModel model) {
    this.radius = radius;
    this.model = model;
  }

  @Override
  protected double[] filterPixel(DoubleImage source, int x, int y, ColorModel colorModel, double[] color) {
    int size = radius+radius+1;
    source.medianPixel(x - radius, y - radius, size, size, colorModel, color);
    return color;
  }

  @Override
  public String toString() {
    return "MedianFilter(radius=" + radius + ")";
  }
}
