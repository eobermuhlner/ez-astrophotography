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
  public double[] getNativePixel(int x, int y, double[] samples) {
    return arrayImage.getNativePixel(x, y, samples);
  }

  @Override
  public void setNativePixel(int x, int y, double[] samples) {
    arrayImage.setNativePixel(x, y, samples);
    image.setNativePixel(x, y, samples);
  }
}
