package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.image.ColorModel;
import ch.obermuhlner.astro.image.ColorUtil;
import ch.obermuhlner.astro.image.DoubleImage;
import javafx.scene.image.WritableImage;

public class WritableDoubleImage implements DoubleImage {

  private final WritableImage image;

  public WritableDoubleImage(WritableImage image) {
    this.image = image;
  }

  @Override
  public int getWidth() {
    return (int) image.getWidth();
  }

  @Override
  public int getHeight() {
    return (int) image.getHeight();
  }

  @Override
  public double[] getPixel(int x, int y, ColorModel model, double[] samples) {
    if (samples == null) {
      samples = new double[3];
    }

    int rgb = image.getPixelReader().getArgb(x, y);

    samples[ColorModel.R] = ((rgb >> 16) & 0xff) / 255.0;
    samples[ColorModel.G] = ((rgb >> 8) & 0xff) / 255.0;
    samples[ColorModel.B] = (rgb & 0xff) / 255.0;

    if (model == ColorModel.HSV) {
      ColorUtil.convertRGBtoHSV(samples[ColorModel.R], samples[ColorModel.G], samples[ColorModel.B], samples);
    }

    return samples;
  }

  @Override
  public void setPixel(int x, int y, ColorModel model, double[] samples) {
    double[] rgbSamples;
    if (model == ColorModel.HSV) {
      rgbSamples = ColorUtil.convertHSVToRGB(samples[ColorModel.H], samples[ColorModel.S], samples[ColorModel.V], null);
    } else {
      rgbSamples = samples;
    }

    int rgb = ColorUtil.toIntRGB(rgbSamples);
    image.getPixelWriter().setArgb(x, y, 0xff000000 | rgb);
  }

}
