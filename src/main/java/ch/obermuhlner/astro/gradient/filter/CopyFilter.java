package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageUtil;

public class CopyFilter implements Filter {

  private final ColorModel model;

  public CopyFilter(ColorModel model) {
    this.model = model;
  }

  @Override
  public void filter(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height) {
    double[] samples = new double[3];
    for (int dy = 0; dy < height; dy++) {
      for (int dx = 0; dx < width; dx++) {
        if (ImageUtil.isInsideImage(source, sourceX + dx, sourceY + dy)) {
          source.getPixel(sourceX + dx, sourceY + dy, model, samples);
        } else {
          samples[0] = 0;
          samples[1] = 0;
          samples[2] = 0;
        }
        if (ImageUtil.isInsideImage(target, targetX + dx, targetY + dy)) {
          target.setPixel(targetX + dx, targetY + dy, model, samples);
        }
      }
    }
  }

  @Override
  public String toString() {
    return "CopyFilter";
  }
}
