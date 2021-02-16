package ch.obermuhlner.astro.image;

import mil.nga.tiff.TiffWriter;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class ImageWriter {

  public static void write(DoubleImage image, File output) throws IOException {
    String name = output.getName();

    for (ImageFormat format : ImageFormat.values()) {
      for (String extension : format.getExtensions()) {
        if (name.length() > extension.length() && name.substring(name.length() - extension.length()).equalsIgnoreCase(extension)) {
          write(image, output, format);
          return;
        }
      }
    }

    write(image, output, ImageFormat.TIF);
  }

  public static void write(DoubleImage image, File output, ImageFormat format) throws IOException {
    if (image instanceof TiffDoubleImage) {
      if (format == ImageFormat.TIF) {
        TiffDoubleImage tiffDoubleImage = (TiffDoubleImage) image;
        TiffWriter.writeTiff(output, tiffDoubleImage.tiffImage);
        return;
      }

      int width = image.getWidth();
      int height = image.getHeight();
      double[] rgb = new double[3];
      DoubleImage imageCopy = ImageCreator.create(width, height, ImageQuality.Standard);
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          image.getPixel(x, y, ColorModel.RGB, rgb);
          imageCopy.setPixel(x, y, ColorModel.RGB, rgb);
        }
      }
      image = imageCopy;
    }

    if (!(image instanceof AwtBufferedDoubleImage)) {
      DoubleImage temp = new AwtBufferedDoubleImage(new BufferedImage(
          image.getWidth(),
          image.getHeight(),
          BufferedImage.TYPE_INT_RGB
      ));
      ImageUtil.copyPixels(image, 0, 0, temp, 0, 0, image.getWidth(), image.getHeight(), ColorModel.RGB);
      image = temp;
    }

    AwtBufferedDoubleImage bufferedDoubleImage = ((AwtBufferedDoubleImage) image);
    ImageIO.write(bufferedDoubleImage.image, format.name(), output);
    return;
  }
}
