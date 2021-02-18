package ch.obermuhlner.astro.gradient.analysis;

import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageUtil;

public class Histogram {

  private final ColorModel colorModel;
  private final int binCount;

  private int sampleCount;
  private final int[][] sampleBins;
  private int maxSampleCountInBin;

  public Histogram(ColorModel colorModel, int binCount) {
    this.colorModel = colorModel;
    this.binCount = binCount;

    sampleBins = new int[3][];
    for (int i = 0; i < 3; i++) {
      sampleBins[i] = new int[binCount];
    }
  }

  public ColorModel getColorModel() {
    return colorModel;
  }

  public int getBinCount() {
    return binCount;
  }

  public double getBin(int sampleIndex, int binIndex) {
    return ((double) sampleBins[sampleIndex][binIndex]) / maxSampleCountInBin;
  }

  public int[] getRawSampleBins(int sampleIndex) {
    return sampleBins[sampleIndex];
  }

  public double[] getSampleBins(int sampleIndex) {
    return getSampleBins(sampleIndex, null);
  }

  public double[] getSampleBins(int sampleIndex, double[] bins) {
    if (bins == null) {
      bins = new double[binCount];
    }

    for (int i = 0; i < binCount; i++) {
      bins[i] = ((double) sampleBins[sampleIndex][i]) / maxSampleCountInBin;
    }

    return bins;
  }

  public void sampleImage(DoubleImage image) {
    sampleImage(image, 0, 0, image.getWidth(), image.getHeight());
  }

  public void sampleImage(DoubleImage image, int x, int y, int width, int height) {
    double[] pixel = new double[3];

    clear();

    for (int iy = 0; iy < height; iy++) {
      for (int ix = 0; ix < width; ix++) {
        if (ImageUtil.isInsideImage(image, x + ix, y + iy)) {
          image.getPixel(x + ix, y + iy, colorModel, pixel);
          addSample(pixel, 0);
          addSample(pixel, 1);
          addSample(pixel, 2);
          sampleCount++;
        }
      }
    }

    maxSampleCountInBin = 0;
    for (int sampleIndex = 0; sampleIndex < sampleBins.length; sampleIndex++) {
      for (int binIndex = 0; binIndex < binCount; binIndex++) {
        maxSampleCountInBin = Math.max(maxSampleCountInBin, sampleBins[sampleIndex][binIndex]);
      }
    }
  }

  public void clear() {
    sampleCount = 0;

    for (int sampleIndex = 0; sampleIndex < sampleBins.length; sampleIndex++) {
      for (int binIndex = 0; binIndex < binCount; binIndex++) {
        sampleBins[sampleIndex][binIndex] = 0;
      }
    }
  }

  private void addSample(double[] pixel, int sampleIndex) {
    double value = pixel[sampleIndex];
    if (colorModel == ColorModel.HSV && sampleIndex == ColorModel.H) {
      value = value / 360.0;
    }
    int binIndex = (int) (value * binCount);
    binIndex = Math.max(0, binIndex);
    binIndex = Math.min(binCount - 1, binIndex);
    sampleBins[sampleIndex][binIndex]++;
  }
}
