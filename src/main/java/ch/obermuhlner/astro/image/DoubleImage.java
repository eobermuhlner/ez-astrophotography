package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.color.ColorUtil;

public interface DoubleImage {

  int getWidth();
  int getHeight();

  double[] getNativePixel(int x, int y, double[] samples);
  void setNativePixel(int x, int y, double[] samples);

  default ColorModel getColorModel() {
    return ColorModel.RGB;
  };

  default double[] getRGB(int x, int y, double[] samples) {
    return getPixel(x, y, ColorModel.RGB, samples);
  }
  default void setRGB(int x, int y, double[] samples) {
    setPixel(x, y, ColorModel.RGB, samples);
  }

  default double[] getPixel(int x, int y, ColorModel model, double[] samples) {
    x = Math.max(0, Math.min(getWidth() - 1, x));
    y = Math.max(0, Math.min(getHeight() - 1, y));

    double[] result = getNativePixel(x, y, samples);
    if (model != getColorModel()) {
      ColorUtil.convert(result, model, getColorModel(), result);
    }
    return result;
  }

  default void setPixel(int x, int y, ColorModel model, double[] samples) {
    if (x >= 0 && y >= 0 && x < getWidth() && y < getHeight()) {
      if (model != getColorModel()) {
        samples = ColorUtil.convert(samples, model, getColorModel(), null);
      }
      setNativePixel(x, y, samples);
    }
  }

  default boolean isInside(int x, int y) {
    return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
  }

}
