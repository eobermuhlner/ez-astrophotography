package ch.obermuhlner.astro.gradient.operation;

import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.color.ColorModel;

public abstract class AbstractPixelImageOperation implements ImageOperation {

  @Override
  public DoubleImage operation(DoubleImage image1, DoubleImage image2, DoubleImage result) {
    double[] samples = new double[3];
    ColorModel colorModel = result.getColorModel();

    for (int y = 0; y < image1.getWidth(); y++) {
      for (int x = 0; x < image2.getHeight(); x++) {
        double[] pixelResult = pixelOperation(image1, image2, x, y, samples);
        result.setPixel(x, y, colorModel, pixelResult);
      }
    }

    return result;
  }

  protected abstract double[] pixelOperation(DoubleImage image1, DoubleImage image2, int x, int y, double[] result);
}
