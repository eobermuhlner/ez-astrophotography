package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;

public class SubDoubleImage implements DoubleImage {

  private final DoubleImage image;
  private final int offsetX;
  private final int offsetY;
  private final int width;
  private final int height;

  public SubDoubleImage(DoubleImage image, int offsetX, int offsetY, int width, int height) {
    this.image = image;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.width = width;
    this.height = height;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public double[] getPixel(int x, int y, ColorModel model, double[] samples) {
    int xx = Math.max(0, Math.min(width - 1, x + offsetX));
    int yy = Math.max(0, Math.min(height - 1, y + offsetY));
    return image.getPixel(xx, yy, model, samples);
  }

  @Override
  public void setPixel(int x, int y, ColorModel model, double[] samples) {
    int xx = x + offsetX;
    int yy = y + offsetY;
    if (xx >= 0 && xx < image.getWidth() && yy >= 0 && yy < image.getHeight()) {
      image.setPixel(xx, yy, model, samples);
    }
  }
}
