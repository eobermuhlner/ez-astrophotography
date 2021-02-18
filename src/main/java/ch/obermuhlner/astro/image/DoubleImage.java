package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;

public interface DoubleImage {

  int getWidth();
  int getHeight();

  double[] getPixel(int x, int y, ColorModel model, double[] samples);
  void setPixel(int x, int y, ColorModel model, double[] samples);
}
