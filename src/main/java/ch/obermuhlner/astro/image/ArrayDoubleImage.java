package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;

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
  public double[] getNativePixel(int x, int y, double[] color) {
    if (color == null) {
      color = new double[3];
    }

    int index = (x + y * width) * SAMPLES_PER_PIXEL;
    color[0] = data[index + 0];
    color[1] = data[index + 1];
    color[2] = data[index + 2];

    return color;
  }

  @Override
  public void setNativePixel(int x, int y, double[] color) {
    int index = (x + y * width) * SAMPLES_PER_PIXEL;

    data[index + 0] = color[0];
    data[index + 1] = color[1];
    data[index + 2] = color[2];
  }
}
