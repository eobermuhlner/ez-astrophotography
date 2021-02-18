package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.color.ColorUtil;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;

public class TiffDoubleImage implements DoubleImage {

  final TIFFImage tiffImage;
  final Rasters image;

  public TiffDoubleImage(TIFFImage image, boolean read) {
    this.tiffImage = image;
    if (read) {
      this.image = image.getFileDirectories().get(0).readRasters();
    } else {
      this.image = image.getFileDirectories().get(0).getWriteRasters();
    }
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

    Number[] pixel = image.getPixel(x, y);
    if (pixel[0] instanceof Double) {
      samples[ColorModel.RGB.R] = (double) pixel[0];
      samples[ColorModel.RGB.G] = (double) pixel[1];
      samples[ColorModel.RGB.B] = (double) pixel[2];
    } else if (pixel[0] instanceof Float) {
      samples[ColorModel.RGB.R] = (float) pixel[0];
      samples[ColorModel.RGB.G] = (float) pixel[1];
      samples[ColorModel.RGB.B] = (float) pixel[2];
    } else {
      samples[ColorModel.RGB.R] = pixel[0].doubleValue() / 256.0;
      samples[ColorModel.RGB.G] = pixel[1].doubleValue() / 256.0;
      samples[ColorModel.RGB.B] = pixel[2].doubleValue() / 256.0;
    }

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

    image.setPixel(x, y, new Float[] { (float) rgbSamples[ColorModel.RGB.R], (float) rgbSamples[ColorModel.RGB.G], (float) rgbSamples[ColorModel.RGB.B] });
  }

}
