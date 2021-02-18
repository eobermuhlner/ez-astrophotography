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
  public double[] getNativePixel(int x, int y, double[] samples) {
    return image.getNativePixel(x + offsetX, y + offsetX, samples);
  }

  @Override
  public void setNativePixel(int x, int y, double[] samples) {
    image.setNativePixel(x + offsetX, y + offsetX, samples);
  }
}
