package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;

import java.util.Arrays;

public class ImageUtil {

  public static double[] medianPixelPerSample(DoubleImage image, ColorModel colorModel, double[] color) {
    if (color == null) {
      color = new double[3];
    }

    int n = image.getWidth() * image.getHeight();
    double[] values0 = new double[n];
    double[] values1 = new double[n];
    double[] values2 = new double[n];

    int index = 0;
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        image.getPixel(x, y, colorModel, color);
        values0[index] = color[0];
        values1[index] = color[1];
        values2[index] = color[2];
        index++;
      }
    }

    Arrays.sort(values0);
    Arrays.sort(values1);
    Arrays.sort(values2);

    if (n % 2 == 0) {
      color[0] = (values0[n/2] + values0[n/2+1]) / 2;
      color[1] = (values1[n/2] + values1[n/2+1]) / 2;
      color[2] = (values2[n/2] + values2[n/2+1]) / 2;
    } else {
      color[0] = values0[n/2];
      color[1] = values1[n/2];
      color[2] = values2[n/2];
    }

    return color;
  }

}
