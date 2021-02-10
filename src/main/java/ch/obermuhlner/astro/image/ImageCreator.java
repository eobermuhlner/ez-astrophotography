package ch.obermuhlner.astro.image;

import mil.nga.tiff.FieldType;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;
import mil.nga.tiff.util.TiffConstants;

import java.awt.image.*;
import java.util.Arrays;

public class ImageCreator {

  public static DoubleImage create(int width, int height, ImageQuality quality) {
    if (quality == ImageQuality.High) {
      return createTiff(width, height);
    }

    return new AwtBufferedDoubleImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));
  }

  public static DoubleImage createTiff(int width, int height) {
    int samplesPerPixel = 3;
    FieldType fieldType = FieldType.FLOAT;
    int bitsPerSample = fieldType.getBits();

    Rasters rasters = new Rasters(width, height, samplesPerPixel, fieldType);

    int rowsPerStrip = rasters.calculateRowsPerStrip(TiffConstants.PLANAR_CONFIGURATION_CHUNKY);

    FileDirectory directory = new FileDirectory();
    directory.setImageWidth(width);
    directory.setImageHeight(height);
    directory.setBitsPerSample(Arrays.asList(bitsPerSample, bitsPerSample, bitsPerSample));
    directory.setCompression(TiffConstants.COMPRESSION_NO);
    directory.setPhotometricInterpretation(TiffConstants.PHOTOMETRIC_INTERPRETATION_RGB);
    directory.setSamplesPerPixel(samplesPerPixel);
    directory.setRowsPerStrip(rowsPerStrip);
    directory.setPlanarConfiguration(TiffConstants.PLANAR_CONFIGURATION_CHUNKY);
    directory.setSampleFormat(TiffConstants.SAMPLE_FORMAT_FLOAT);
    directory.setWriteRasters(rasters);

    TIFFImage tiffImage = new TIFFImage();
    tiffImage.add(directory);

    return new TiffDoubleImage(tiffImage, false);
  }
}