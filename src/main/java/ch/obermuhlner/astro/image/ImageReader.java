package ch.obermuhlner.astro.image;

import mil.nga.tiff.TiffReader;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ImageReader {

  public static DoubleImage read(File file) throws IOException {
    return read(file, ImageQuality.High);
  }

  public static DoubleImage read(File file, ImageQuality quality) throws IOException {
    if (quality == ImageQuality.High) {
      try {
        return new TiffDoubleImage(TiffReader.readTiff(file), true);
      } catch (Exception ex) {
        // ignore
      }
    }

    return new AwtBufferedDoubleImage(ImageIO.read(file));
  }
}
