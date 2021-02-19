package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.color.ColorModel;

import java.util.Arrays;
import java.util.Comparator;

public class VerticalMedianFilter implements Filter {

  private final int radius;
  private final ColorModel model;

  public VerticalMedianFilter(int radius, ColorModel model) {
    this.radius = radius;
    this.model = model;
  }

  @Override
  public DoubleImage filter(DoubleImage source, DoubleImage target, int width, int height) {
    double[][] data = new double[height][3];

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int n = 0;
        for (int dy = -radius; dy <= radius; dy++) {
          int yy = y + dy;
          if (source.isReallyInside(x, yy)) {
            source.getPixel(x, yy, model, data[n++]);
          }
        }
        Arrays.sort(data, 0, n, Comparator.<double[]>
            comparingDouble(c -> c[2])
            .thenComparing(c -> c[1])
            .thenComparing(c -> c[0]));

        target.setPixel(x, y, model, data[n/2]);
      }
    }
    return target;
  }

  @Override
  public String toString() {
    return "VerticalMedianFilter(radius=" + radius + ")";
  }
}
