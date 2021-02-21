package ch.obermuhlner.astro.image;

import ch.obermuhlner.astro.image.color.ColorModel;
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
  public double[] getNativePixel(int x, int y, double[] color) {
    if (color == null) {
      color = new double[3];
    }

    Number[] pixel = image.getPixel(x, y);
    if (pixel[0] instanceof Double) {
      color[ColorModel.RGB.R] = (double) pixel[0];
      color[ColorModel.RGB.G] = (double) pixel[1];
      color[ColorModel.RGB.B] = (double) pixel[2];
    } else if (pixel[0] instanceof Float) {
      color[ColorModel.RGB.R] = (float) pixel[0];
      color[ColorModel.RGB.G] = (float) pixel[1];
      color[ColorModel.RGB.B] = (float) pixel[2];
    } else {
      color[ColorModel.RGB.R] = pixel[0].doubleValue() / 256.0;
      color[ColorModel.RGB.G] = pixel[1].doubleValue() / 256.0;
      color[ColorModel.RGB.B] = pixel[2].doubleValue() / 256.0;
    }

    return color;
  }

  @Override
  public void setNativePixel(int x, int y, double[] color) {
    image.setPixel(x, y, new Float[] { (float) color[ColorModel.RGB.R], (float) color[ColorModel.RGB.G], (float) color[ColorModel.RGB.B] });
  }

}
