package ch.obermuhlner.astro.image;

import java.awt.image.*;

public class BufferedDoubleImage implements DoubleImage {

  final BufferedImage image;

  public BufferedDoubleImage(BufferedImage image) {
    this.image = image;
  }

  @Override
  public int getWidth() {
    return image.getWidth();
  }

  @Override
  public int getHeight() {
    return image.getHeight();
  }

  @Override
  public RGBColor getPixel(int x, int y) {
    return RGBColor.fromIntRGB(image.getRGB(x, y));
  }

  @Override
  public void setPixel(int x, int y, RGBColor rgb) {
    image.setRGB(x, y, rgb.toIntRGB());
  }
}
