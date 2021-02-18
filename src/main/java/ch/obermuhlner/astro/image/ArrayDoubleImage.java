package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.color.ColorUtil;

public class ArrayDoubleImage implements DoubleImage {

  private static final int SAMPLES_PER_PIXEL = 3;

  private final int width;
  private final int height;
  private final ColorModel colorModel;
  private final double[] data;

  public ArrayDoubleImage(int width, int height, ColorModel colorModel) {
    this.width = width;
    this.height = height;
    this.colorModel = colorModel;

    data = new double[width * height * SAMPLES_PER_PIXEL];
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  public ColorModel getColorModel() {
    return colorModel;
  }

  @Override
  public double[] getPixel(int x, int y, ColorModel model, double[] samples) {
    if (samples == null) {
      samples = new double[3];
    }

    int index = (x + y * width) * SAMPLES_PER_PIXEL;
    samples[0] = data[index + 0];
    samples[1] = data[index + 1];
    samples[2] = data[index + 2];

    if (ColorModel.RGB.equals(colorModel)) {
      if (ColorModel.HSV.equals(model)) {
        ColorUtil.convertRGBtoHSV(samples, samples);
      }
    }
    else if (ColorModel.HSV.equals(colorModel)) {
      if (ColorModel.RGB.equals(model)) {
        ColorUtil.convertHSVtoRGB(samples, samples);
      }
    }
    else {
      throw new IllegalArgumentException("Unknown: " + colorModel);
    }

    return samples;
  }

  @Override
  public void setPixel(int x, int y, ColorModel model, double[] samples) {
    int index = (x + y * width) * SAMPLES_PER_PIXEL;

    data[index + 0] = samples[0];
    data[index + 1] = samples[1];
    data[index + 2] = samples[2];

    if (ColorModel.RGB.equals(colorModel)) {
      if (ColorModel.HSV.equals(model)) {
        ColorUtil.convertRGBtoHSV(data, data, index, index);
      }
    }
    else if (ColorModel.HSV.equals(colorModel)) {
      if (ColorModel.RGB.equals(model)) {
        ColorUtil.convertHSVtoRGB(data, data, index, index);
      }
    }
    else {
      throw new IllegalArgumentException("Unknown: " + colorModel);
    }
  }
}
