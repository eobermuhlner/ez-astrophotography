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

}
