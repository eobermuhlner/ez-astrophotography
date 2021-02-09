package ch.obermuhlner.astro.image;

import mil.nga.tiff.TiffReader;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ImageReader {

  public static DoubleImage read(File file) throws IOException {
    try {
      return new TiffDoubleImage(TiffReader.readTiff(file), true);
    } catch (Exception ex) {
      return new BufferedDoubleImage(ImageIO.read(file));
    }
  }
}
