package ch.obermuhlner.astro.image.color;

public interface ColorModel {

  ColorModelRGB RGB = new ColorModelRGB();
  ColorModelHSV HSV = new ColorModelHSV();

  class ColorModelRGB implements ColorModel {
    public static final int R = 0;
    public static final int G = 1;
    public static final int B = 2;
  }

  class ColorModelHSV implements ColorModel {
    public static final int H = 0;
    public static final int S = 1;
    public static final int V = 2;
  }
}
