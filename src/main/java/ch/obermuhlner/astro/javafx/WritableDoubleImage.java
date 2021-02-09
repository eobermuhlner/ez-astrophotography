package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.RGBColor;
import javafx.scene.image.WritableImage;

public class WritableDoubleImage implements DoubleImage {

  private final WritableImage image;

  public WritableDoubleImage(WritableImage image) {
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
  public RGBColor getPixel(int x, int y) {
    return RGBColor.fromIntRGB(image.getPixelReader().getArgb(x, y));
  }

  @Override
  public void setPixel(int x, int y, RGBColor rgb) {
    image.getPixelWriter().setArgb(x, y, 0xff000000 | rgb.toIntRGB());
  }
}
