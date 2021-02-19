package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.color.ColorUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

  default double[] getPixel(int x, int y, ColorModel colorModel, double[] samples) {
    int xx = Math.max(0, Math.min(getWidth() - 1, x));
    int yy = Math.max(0, Math.min(getHeight() - 1, y));

    double[] result = getNativePixel(xx, yy, samples);
    if (colorModel != getColorModel()) {
      ColorUtil.convert(result, colorModel, result, getColorModel());
    }
    return result;
  }

  default void setPixel(int x, int y, ColorModel colorModel, double[] samples) {
    if (isInside(x, y)) {
      if (colorModel != getColorModel()) {
        samples = ColorUtil.convert(samples, colorModel, null, getColorModel());
      }
      setNativePixel(x, y, samples);
    }
  }

  default void setPixels(ColorModel colorModel, double[] fillColor) {
    for (int y = 0; y < getHeight(); y++) {
      for (int x = 0; x < getWidth(); x++) {
        setPixel(x, y, colorModel, fillColor);
      }
    }
  }

  default void setPixels(int x, int y, int width, int height, ColorModel colorModel, double[] fillColor) {
    subImage(x, y, width, height).setPixels(colorModel, fillColor);
  }

  default void setPixels(int sourceX, int sourceY, DoubleImage source, int targetX, int targetY, int width, int height, ColorModel colorModel, double[] outsideColor) {
    subImage(targetX, targetY, width, height).setPixels(source.subImage(sourceX, sourceY, width, height), colorModel, outsideColor);
  }

  default void setPixels(DoubleImage source, ColorModel colorModel, double[] outsideColor) {
    double[] samples = new double[3];
    for (int y = 0; y < getHeight(); y++) {
      for (int x = 0; x < getWidth(); x++) {
        if (outsideColor == null || source.isInside(x, y)) {
          source.getPixel(x, y, colorModel, samples);
        } else {
          samples[0] = outsideColor[0];
          samples[1] = outsideColor[1];
          samples[2] = outsideColor[2];
        }
        setPixel(x, y, colorModel, samples);
      }
    }
  }

  default boolean isInside(int x, int y) {
    return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
  }

  default boolean isReallyInside(int x, int y) {
    return isInside(x, y);
  }

  default DoubleImage subImage(int x, int y, int width, int height) {
    return new SubDoubleImage(this, x, y, width, height);
  }

  default double[] averagePixel(int x, int y, int width, int height, ColorModel colorModel, double[] color) {
    return subImage(x, y, width, height).averagePixel(colorModel, color);
  }

  default double[] averagePixel(ColorModel colorModel, double[] color) {
    if (color == null) {
      color = new double[3];
    }

    int n = 0;
    double sample0 = 0;
    double sample1 = 0;
    double sample2 = 0;
    for (int y = 0; y < getHeight(); y++) {
      for (int x = 0; x < getWidth(); x++) {
        if (isReallyInside(x, y)) {
          getPixel(x, y, colorModel, color);
          sample0 += color[0];
          sample1 += color[1];
          sample2 += color[2];
          n++;
        }
      }
    }
    color[0] = sample0 / n;
    color[1] = sample1 / n;
    color[2] = sample2 / n;
    return color;
  }

  default double[] medianPixel(int x, int y, int width, int height, ColorModel colorModel, double[] color) {
    return subImage(x, y, width, height).medianPixel(colorModel, color);
  }

  default double[] medianPixel(ColorModel colorModel, double[] color) {
    if (color == null) {
      color = new double[3];
    }

    List<double[]> data = new ArrayList<>(getWidth() * getHeight());
    for (int y = 0; y < getHeight(); y++) {
      for (int x = 0; x < getWidth(); x++) {
        if (isReallyInside(x, y)) {
          double[] sample = getPixel(x, y, ColorModel.HSV, null);
          data.add(sample);
        }
      }
    }

    Collections.sort(data, Comparator.<double[]>
        comparingDouble(c -> c[ColorModel.HSV.V])
        .thenComparing(c -> c[ColorModel.HSV.S])
        .thenComparing(c -> c[ColorModel.HSV.H]));

    int n = data.size();
    int nHalf = n / 2;
    int nHalfPlus1 = nHalf + 1;
    if (n % 2 == 0) {
      color[0] = (data.get(nHalf)[ColorModel.HSV.H] + data.get(nHalfPlus1)[ColorModel.HSV.H]) / 2;
      color[1] = (data.get(nHalf)[ColorModel.HSV.S] + data.get(nHalfPlus1)[ColorModel.HSV.S]) / 2;
      color[2] = (data.get(nHalf)[ColorModel.HSV.V] + data.get(nHalfPlus1)[ColorModel.HSV.V]) / 2;
    } else {
      color[0] = data.get(nHalf)[ColorModel.HSV.H];
      color[1] = data.get(nHalf)[ColorModel.HSV.S];
      color[2] = data.get(nHalf)[ColorModel.HSV.V];
    }

    return ColorUtil.convert(color, ColorModel.HSV, color, colorModel);
  }

}
