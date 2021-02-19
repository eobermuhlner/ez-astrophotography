package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.color.ColorModel;

public class MedianBlurFilter extends AbstractFilter {

  private final int radius;
  private final ColorModel model;

  public MedianBlurFilter(int radius, ColorModel model) {
    this.radius = radius;
    this.model = model;
  }

  @Override
  protected double[] filterPixel(DoubleImage source, int x, int y, ColorModel colorModel, double[] samples) {
    int size = radius+radius+1;
    source.medianPixel(x - radius, y - radius, size, size, colorModel, samples);
    return samples;
  }

  @Override
  public String toString() {
    return "MedianBlur(radius=" + radius + ")";
  }
}
