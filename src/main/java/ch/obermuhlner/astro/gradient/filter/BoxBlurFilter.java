package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;

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
  public void filter(DoubleImage source, DoubleImage target, int width, int height) {
    double[] samples = new double[3];
    for (int dy = 0; dy < height; dy++) {
      for (int dx = 0; dx < width; dx++) {
        double sum0 = 0;
        double sum1 = 0;
        double sum2 = 0;
        for (int kx = dx-radius; kx < dx+radius; kx++) {
          for (int ky = dy-radius; ky < dy+radius; ky++) {
            source.getPixel(kx, ky, model, samples);
            sum0 += samples[0];
            sum1 += samples[1];
            sum2 += samples[2];
          }
        }
        samples[0] = sum0 / kernelSize;
        samples[1] = sum1 / kernelSize;
        samples[2] = sum2 / kernelSize;
        target.setPixel(dx, dy, model, samples);
      }
    }
  }

  @Override
  public String toString() {
    return "BoxBlur(radius=" + radius + ")";
  }
}
