package ch.obermuhlner.astro.image;

public class ArrayDoubleImage implements DoubleImage {

  private static final int samplesPerPixel = 3;

  private final int width;
  private final int height;
  private final ColorModel colorModel;
  private final double[] data;

  public ArrayDoubleImage(int width, int height, ColorModel colorModel) {
    this.width = width;
    this.height = height;
    this.colorModel = colorModel;

    data = new double[width * height * samplesPerPixel];
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

    int index = x + y * width * samplesPerPixel;
    samples[0] = data[index + 0];
    samples[1] = data[index + 1];
    samples[2] = data[index + 2];

    switch (colorModel) {
      case RGB:
        switch (model) {
          case RGB:
            // do nothing
            break;
          case HSV:
            ColorUtil.convertRGBtoHSV(samples, samples);
            break;
        }
        break;
      case HSV:
        switch (model) {
          case RGB:
            ColorUtil.convertHSVtoRGB(samples, samples);
            break;
          case HSV:
            // do nothing
            break;
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown: " + colorModel);
    }

    return samples;
  }

  @Override
  public void setPixel(int x, int y, ColorModel model, double[] samples) {
    int index = x + y * width * samplesPerPixel;

    data[index + 0] = samples[0];
    data[index + 1] = samples[1];
    data[index + 2] = samples[2];

    switch (colorModel) {
      case RGB:
        switch (model) {
          case RGB:
            // do nothing
            break;
          case HSV:
            ColorUtil.convertRGBtoHSV(data, data, index, index);
            break;
        }
        break;
      case HSV:
        switch (model) {
          case RGB:
            ColorUtil.convertHSVtoRGB(data, data, index, index);
            break;
          case HSV:
            // do nothing
            break;
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown: " + colorModel);
    }
  }
}
