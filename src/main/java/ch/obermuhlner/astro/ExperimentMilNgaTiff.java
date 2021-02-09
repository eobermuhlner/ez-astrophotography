package ch.obermuhlner.astro;

import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;
import mil.nga.tiff.TiffReader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ExperimentMilNgaTiff {
  public static void main(String[] args) {
    try {
      File input = new File("images/Autosave001.tif");
      //File input = new File("images/Autosave001.tif_gradient.tif");
      TIFFImage tiffImage = TiffReader.readTiff(input);
      List<FileDirectory> directories = tiffImage.getFileDirectories();
      FileDirectory directory = directories.get(0);
      Rasters rasters = directory.readRasters();

      System.out.println("bits per sample: " + rasters.getBitsPerSample());
      System.out.println("num pixels: " + rasters.getNumPixels());
      System.out.println(Arrays.toString(rasters.getPixel(0, 0)));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
