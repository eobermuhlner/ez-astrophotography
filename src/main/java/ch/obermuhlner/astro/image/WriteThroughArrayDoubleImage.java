package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;

public class WriteThroughArrayDoubleImage implements DoubleImage {

  private final DoubleImage image;
  private final ArrayDoubleImage arrayImage;

  public WriteThroughArrayDoubleImage(DoubleImage image, ColorModel colorModel) {
    this.image = image;

    this.arrayImage = new ArrayDoubleImage(image.getWidth(), image.getHeight(), colorModel);
  }

  @Override
  public int getWidth() {
    return arrayImage.getWidth();
  }

  @Override
  public int getHeight() {
    return arrayImage.getHeight();
  }

  @Override
  public double[] getNativePixel(int x, int y, double[] color) {
    return arrayImage.getNativePixel(x, y, color);
  }

  @Override
  public void setNativePixel(int x, int y, double[] color) {
    arrayImage.setNativePixel(x, y, color);
    image.setNativePixel(x, y, color);
  }
}
