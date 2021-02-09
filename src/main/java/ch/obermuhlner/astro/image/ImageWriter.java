package ch.obermuhlner.astro.image;

import mil.nga.tiff.TiffWriter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ImageWriter {

  public static void writeTif(DoubleImage image, File output) throws IOException {
    if (image instanceof BufferedDoubleImage) {
      BufferedDoubleImage bufferedDoubleImage = ((BufferedDoubleImage) image);
      ImageIO.write(bufferedDoubleImage.image, "TIF", output);
      return;
    }

    if (image instanceof TiffDoubleImage) {
      TiffDoubleImage tiffDoubleImage = (TiffDoubleImage) image;
      TiffWriter.writeTiff(output, tiffDoubleImage.tiffImage);
      return;
    }

    throw new IllegalArgumentException("Unknown image type: " + image.getClass());
  }
}
