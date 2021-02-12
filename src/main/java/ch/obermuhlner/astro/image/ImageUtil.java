package ch.obermuhlner.astro.image;

public class ImageUtil {

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

  public static double[] averagePixel(DoubleImage image, int x, int y, int sampleRadius, ColorModel colorModel) {
    int n = 0;
    double sample0 = 0;
    double sample1 = 0;
    double sample2 = 0;
    double[] color = new double[3];
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
    return new double[] { sample0/n, sample1/n, sample2/n };
  }
}
