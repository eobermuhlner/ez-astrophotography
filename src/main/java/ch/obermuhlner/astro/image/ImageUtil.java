package ch.obermuhlner.astro.image;

import java.util.Arrays;

public class ImageUtil {

  public static void copyPixels(DoubleImage source, DoubleImage target, ColorModel model) {
    copyPixels(source, 0, 0, target, 0, 0, source.getWidth(), source.getHeight(), model);
  }

  public static void copyPixels(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height, ColorModel model) {
    double[] samples = new double[3];
    for (int dy = 0; dy < height; dy++) {
      for (int dx = 0; dx < width; dx++) {
        if (isInsideImage(source, sourceX + dx, sourceY + dy)) {
          source.getPixel(sourceX + dx, sourceY + dy, model, samples);
        } else {
          samples[0] = 0;
          samples[1] = 0;
          samples[2] = 0;
        }
        if (isInsideImage(target, targetX + dx, targetY + dy)) {
          target.setPixel(targetX + dx, targetY + dy, model, samples);
        }
      }
    }
  }

  public static boolean isInsideImage(DoubleImage image, int x, int y) {
    return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
  }

  public static double[] averagePixel(DoubleImage image, int x, int y, int sampleRadius, ColorModel colorModel, double[] color) {
    if (color == null) {
      color = new double[3];
    }

    int n = 0;
    double sample0 = 0;
    double sample1 = 0;
    double sample2 = 0;
    for (int sy = y-sampleRadius; sy <= y+sampleRadius; sy++) {
      for (int sx = x-sampleRadius; sx < x+sampleRadius; sx++) {
        if (ImageUtil.isInsideImage(image, sx, sy)) {
          image.getPixel(sx, sy, colorModel, color);
          sample0 += color[0];
          sample1 += color[1];
          sample2 += color[2];
          n++;
        }
      }
    }

    color[0] = sample0 / n;
    color[1] = sample1 / n;
    color[2] = sample2 / n;
    return color;
  }

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
