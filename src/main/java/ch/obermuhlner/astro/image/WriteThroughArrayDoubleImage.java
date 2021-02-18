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
  public double[] getPixel(int x, int y, ColorModel model, double[] samples) {
    return arrayImage.getPixel(x, y, model, samples);
  }

  @Override
  public void setPixel(int x, int y, ColorModel model, double[] samples) {
    arrayImage.setPixel(x, y, model, samples);
    image.setPixel(x, y, model, samples);
  }
}
