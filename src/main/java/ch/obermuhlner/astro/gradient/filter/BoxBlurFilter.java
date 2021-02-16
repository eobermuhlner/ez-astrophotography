package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageUtil;

public class BoxBlurFilter implements Filter {

  private final int radius;
  private final ColorModel model;
  private final int kernelSize;

  public BoxBlurFilter(int radius, ColorModel model) {
    this.radius = radius;
    this.model = model;
    this.kernelSize = (radius+radius+1) * (radius+radius+1);
  }

  @Override
  public void filter(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height) {
    double[] samples = new double[3];
    for (int dy = 0; dy < height; dy++) {
      for (int dx = 0; dx < width; dx++) {
        double sum0 = 0;
        double sum1 = 0;
        double sum2 = 0;
        for (int kx = dx-radius; kx < dx+radius; kx++) {
          for (int ky = dy-radius; ky < dy+radius; ky++) {
            int sx = Math.min(sourceX+width - 1, Math.max(sourceX, kx));
            int sy = Math.min(sourceY+height - 1, Math.max(sourceY, ky));
            source.getPixel(sx, sy, model, samples);
            sum0 += samples[0];
            sum1 += samples[1];
            sum2 += samples[2];
          }
          int tx = targetX + dx;
          int ty = targetY + dy;
          if (ImageUtil.isInsideImage(target, tx, ty)) {
            samples[0] = sum0 / kernelSize;
            samples[1] = sum1 / kernelSize;
            samples[2] = sum2 / kernelSize;
            target.setPixel(tx, ty, model, samples);
          }
        }
      }
    }
  }

  @Override
  public String toString() {
    return "BoxBlur(radius=" + radius + ")";
  }
}
