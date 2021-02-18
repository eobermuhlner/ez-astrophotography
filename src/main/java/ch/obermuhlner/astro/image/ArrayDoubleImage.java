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

  @Override
  public ColorModel getColorModel() {
    return colorModel;
  }

  @Override
  public double[] getNativePixel(int x, int y, double[] samples) {
    if (samples == null) {
      samples = new double[3];
    }

    int index = (x + y * width) * SAMPLES_PER_PIXEL;
    samples[0] = data[index + 0];
    samples[1] = data[index + 1];
    samples[2] = data[index + 2];

    return samples;
  }

  @Override
  public void setNativePixel(int x, int y, double[] samples) {
    int index = (x + y * width) * SAMPLES_PER_PIXEL;

    data[index + 0] = samples[0];
    data[index + 1] = samples[1];
    data[index + 2] = samples[2];
  }
}
