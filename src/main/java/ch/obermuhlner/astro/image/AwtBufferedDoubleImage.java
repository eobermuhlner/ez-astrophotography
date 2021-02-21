package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.color.ColorUtil;

import java.awt.image.*;

public class AwtBufferedDoubleImage implements DoubleImage {

  final BufferedImage image;

  public AwtBufferedDoubleImage(BufferedImage image) {
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

  public double[] getNativePixel(int x, int y, double[] color) {
    if (color == null) {
      color = new double[3];
    }

    int rgb = image.getRGB(x, y);

    color[ch.obermuhlner.astro.image.color.ColorModel.RGB.R] = ((rgb >> 16) & 0xff) / 255.0;
    color[ch.obermuhlner.astro.image.color.ColorModel.RGB.G] = ((rgb >> 8) & 0xff) / 255.0;
    color[ch.obermuhlner.astro.image.color.ColorModel.RGB.B] = (rgb & 0xff) / 255.0;

    return color;
  }

  @Override
  public void setNativePixel(int x, int y, double[] color) {
    image.setRGB(x, y, ColorUtil.toIntRGB(color));
  }
}
