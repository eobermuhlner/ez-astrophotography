package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.color.ColorModel;

public abstract class AbstractFilter implements Filter {

  private final ColorModel defaultColorModel;

  public AbstractFilter() {
    this(null);
  }

  public AbstractFilter(ColorModel colorModel) {
    this.defaultColorModel = colorModel;
  }

  @Override
  public DoubleImage filter(DoubleImage source, DoubleImage target, int width, int height) {
    ColorModel colorModel = defaultColorModel == null ? target.getColorModel() : defaultColorModel;

    double[] color = new double[3];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        target.setPixel(x, y, colorModel, filterPixel(source, x, y, colorModel, color));
      }
    }
    return target;
  }

  protected abstract double[] filterPixel(DoubleImage source, int x, int y, ColorModel colorModel, double[] color);
}
