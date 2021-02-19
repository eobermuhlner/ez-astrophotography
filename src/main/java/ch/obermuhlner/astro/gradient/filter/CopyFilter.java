package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;

public class CopyFilter implements Filter {

  private final ColorModel model;

  public CopyFilter(ColorModel model) {
    this.model = model;
  }

  @Override
  public DoubleImage filter(DoubleImage source, DoubleImage target, int width, int height) {
    double[] samples = new double[3];
    for (int dy = 0; dy < height; dy++) {
      for (int dx = 0; dx < width; dx++) {
        if (source.isInside(dx, dy)) {
          source.getPixel(dx, dy, model, samples);
        } else {
          samples[0] = 0;
          samples[1] = 0;
          samples[2] = 0;
        }

        target.setPixel(dx, dy, model, samples);
      }
    }
    return target;
  }

  @Override
  public String toString() {
    return "CopyFilter";
  }
}
