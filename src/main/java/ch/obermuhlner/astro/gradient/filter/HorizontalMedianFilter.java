package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.color.ColorModel;

import java.util.Arrays;
import java.util.Comparator;

public class HorizontalMedianFilter implements Filter {

  private final int radius;
  private final ColorModel model;

  public HorizontalMedianFilter(int radius, ColorModel model) {
    this.radius = radius;
    this.model = model;
  }

  @Override
  public DoubleImage filter(DoubleImage source, DoubleImage target, int width, int height) {
    double[][] data = new double[width][3];

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int n = 0;
        for (int dx = -radius; dx <= radius; dx++) {
          int xx = x + dx;
          if (source.isReallyInside(xx, y)) {
            source.getPixel(xx, y, model, data[n++]);
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
    return "HorizontalMedianFilter(radius=" + radius + ")";
  }
}
