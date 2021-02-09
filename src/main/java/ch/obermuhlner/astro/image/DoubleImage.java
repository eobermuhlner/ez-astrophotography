package ch.obermuhlner.astro.image;

public interface DoubleImage {

  int getWidth();
  int getHeight();

  RGBColor getPixel(int x, int y);

  void setPixel(int x, int y, RGBColor rgb);
}
