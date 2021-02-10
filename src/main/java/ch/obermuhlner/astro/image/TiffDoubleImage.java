package ch.obermuhlner.astro.image;

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
      samples[ColorModel.R] = (double) pixel[0];
      samples[ColorModel.G] = (double) pixel[1];
      samples[ColorModel.B] = (double) pixel[2];
    } else if (pixel[0] instanceof Float) {
      samples[ColorModel.R] = (float) pixel[0];
      samples[ColorModel.G] = (float) pixel[1];
      samples[ColorModel.B] = (float) pixel[2];
    } else {
      samples[ColorModel.R] = pixel[0].doubleValue() / 256.0;
      samples[ColorModel.G] = pixel[1].doubleValue() / 256.0;
      samples[ColorModel.B] = pixel[2].doubleValue() / 256.0;
    }

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

    image.setPixel(x, y, new Float[] { (float) rgbSamples[ColorModel.R], (float) rgbSamples[ColorModel.G], (float) rgbSamples[ColorModel.B] });
  }

}
