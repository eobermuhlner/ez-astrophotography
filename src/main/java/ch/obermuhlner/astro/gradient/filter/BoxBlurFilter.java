package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;

public class BoxBlurFilter extends AbstractFilter {

  private final int radius;
  private final ColorModel model;
  private final int kernelSize;

  public BoxBlurFilter(int radius, ColorModel model) {
    this.radius = radius;
    this.model = model;
    this.kernelSize = (radius+radius+1) * (radius+radius+1);
  }

  @Override
  protected double[] filterPixel(DoubleImage source, int x, int y, ColorModel colorModel, double[] color) {
    double sum0 = 0;
    double sum1 = 0;
    double sum2 = 0;
    for (int kx = x-radius; kx < x+radius; kx++) {
      for (int ky = y-radius; ky < y+radius; ky++) {
        source.getPixel(kx, ky, model, color);
        sum0 += color[0];
        sum1 += color[1];
        sum2 += color[2];
      }
    }
    color[0] = sum0 / kernelSize;
    color[1] = sum1 / kernelSize;
    color[2] = sum2 / kernelSize;
    return color;
  }


  @Override
  public String toString() {
    return "BoxBlur(radius=" + radius + ")";
  }
}
