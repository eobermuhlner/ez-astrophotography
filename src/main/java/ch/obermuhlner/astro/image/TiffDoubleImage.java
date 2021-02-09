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
  public RGBColor getPixel(int x, int y) {
    Number[] pixel = image.getPixel(x, y);
    if (pixel[0] instanceof Double) {
      double r = (double) pixel[0];
      double g = (double) pixel[1];
      double b = (double) pixel[2];
      return new RGBColor(r, g, b);
    }

    if (pixel[0] instanceof Float) {
      double r = (float) pixel[0];
      double g = (float) pixel[1];
      double b = (float) pixel[2];
      return new RGBColor(r, g, b);
    }

    double r = pixel[0].doubleValue() / 256.0;
    double g = pixel[1].doubleValue() / 256.0;
    double b = pixel[2].doubleValue() / 256.0;
    return new RGBColor(r, g, b);
  }

  @Override
  public void setPixel(int x, int y, RGBColor rgb) {
    image.setPixel(x, y, new Float[] { (float) rgb.r, (float) rgb.g, (float) rgb.b });
  }
}
