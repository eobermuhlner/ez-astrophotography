package ch.obermuhlner.astro.image;

import java.awt.image.*;

public class AwtBufferedDoubleImage implements DoubleImage {

  final BufferedImage image;

  public AwtBufferedDoubleImage(BufferedImage image) {
    this.image = image;
  }

  @Override
  public int getWidth() {
    return image.getWidth();
  }

  @Override
  public int getHeight() {
    return image.getHeight();
  }

  @Override
  public double[] getPixel(int x, int y, ColorModel model, double[] samples) {
    if (samples == null) {
      samples = new double[3];
    }

    int rgb = image.getRGB(x, y);

    samples[ColorModel.R] = ((rgb >> 16) & 0xff) / 255.0;
    samples[ColorModel.G] = ((rgb >> 8) & 0xff) / 255.0;
    samples[ColorModel.B] = (rgb & 0xff) / 255.0;

    if (model == ColorModel.HSV) {
        ColorUtil.convertRGBtoHSV(samples, samples);
    }

    return samples;
  }

  @Override
  public void setPixel(int x, int y, ColorModel model, double[] samples) {
    double[] rgbSamples;
    if (model == ColorModel.HSV) {
      rgbSamples = ColorUtil.convertHSVtoRGB(samples, null);
    } else {
      rgbSamples = samples;
    }

    int rgb = ColorUtil.toIntRGB(rgbSamples);
    image.setRGB(x, y, rgb);
  }
}
