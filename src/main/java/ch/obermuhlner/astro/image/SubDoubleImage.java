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
  public ColorModel getColorModel() {
    return image.getColorModel();
  }

  @Override
  public boolean isReallyInside(int x, int y) {
    return isInside(x, y) && image.isInside(x + offsetX, y + offsetY);
  }

  @Override
  public double[] getNativePixel(int x, int y, double[] color) {
    int xx = Math.max(0, Math.min(image.getWidth() - 1, x + offsetX));
    int yy = Math.max(0, Math.min(image.getHeight() - 1, y + offsetY));

    return image.getNativePixel(xx, yy, color);
  }

  @Override
  public void setNativePixel(int x, int y, double[] color) {
    int xx = x + offsetX;
    int yy = y + offsetY;

    if (image.isInside(xx, yy)) {
      image.setNativePixel(x + offsetX, y + offsetX, color);
    }
  }
}
