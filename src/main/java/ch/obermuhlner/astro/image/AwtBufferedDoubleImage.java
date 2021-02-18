package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.color.ColorUtil;

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
  public double[] getPixel(int x, int y, ch.obermuhlner.astro.image.color.ColorModel model, double[] samples) {
    if (samples == null) {
      samples = new double[3];
    }

    int rgb = image.getRGB(x, y);

    samples[ch.obermuhlner.astro.image.color.ColorModel.R] = ((rgb >> 16) & 0xff) / 255.0;
    samples[ch.obermuhlner.astro.image.color.ColorModel.G] = ((rgb >> 8) & 0xff) / 255.0;
    samples[ch.obermuhlner.astro.image.color.ColorModel.B] = (rgb & 0xff) / 255.0;

    if (model == ch.obermuhlner.astro.image.color.ColorModel.HSV) {
        ColorUtil.convertRGBtoHSV(samples, samples);
    }

    return samples;
  }

  @Override
  public void setPixel(int x, int y, ch.obermuhlner.astro.image.color.ColorModel model, double[] samples) {
    double[] rgbSamples;
    if (model == ColorModel.HSV) {
      rgbSamples = ColorUtil.convertHSVtoRGB(samples, null);
    } else {
      rgbSamples = samples;
    }

    image.setRGB(x, y, ColorUtil.toIntRGB(rgbSamples));
  }
}
