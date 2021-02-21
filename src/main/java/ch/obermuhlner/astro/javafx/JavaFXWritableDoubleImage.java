package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.color.ColorUtil;
import ch.obermuhlner.astro.image.DoubleImage;
import javafx.scene.image.WritableImage;

public class JavaFXWritableDoubleImage implements DoubleImage {

  private final WritableImage image;

  public JavaFXWritableDoubleImage(WritableImage image) {
    this.image = image;
  }

  @Override
  public int getWidth() {
    return (int) image.getWidth();
  }

  @Override
  public int getHeight() {
    return (int) image.getHeight();
  }

  @Override
  public double[] getNativePixel(int x, int y, double[] color) {
    if (color == null) {
      color = new double[3];
    }

    int rgb = image.getPixelReader().getArgb(x, y);

    color[ColorModel.RGB.R] = ((rgb >> 16) & 0xff) / 255.0;
    color[ColorModel.RGB.G] = ((rgb >> 8) & 0xff) / 255.0;
    color[ColorModel.RGB.B] = (rgb & 0xff) / 255.0;

    return color;
  }

  @Override
  public void setNativePixel(int x, int y, double[] color) {
    image.getPixelWriter().setArgb(x, y, ColorUtil.toIntARGB(color));
  }

}
