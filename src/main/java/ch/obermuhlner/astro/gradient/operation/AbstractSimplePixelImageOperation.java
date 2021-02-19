package ch.obermuhlner.astro.gradient.operation;

import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.color.ColorModel;

public abstract class AbstractSimplePixelImageOperation implements ImageOperation {

  @Override
  public DoubleImage operation(DoubleImage image1, DoubleImage image2, DoubleImage result) {
    double[] pixel1 = new double[3];
    double[] pixel2 = new double[3];
    double[] pixelResult = new double[3];
    ColorModel colorModel = result.getColorModel();

    for (int y = 0; y < image1.getHeight(); y++) {
      for (int x = 0; x < image1.getWidth(); x++) {
        image1.getPixel(x, y, colorModel, pixel1);
        image2.getPixel(x, y, colorModel, pixel2);
        result.setPixel(x, y, colorModel, pixelOperation(pixel1, pixel2, x, y, pixelResult));
      }
    }

    return result;
  }

  protected abstract double[] pixelOperation(double[] pixel1, double[] pixel2, int x, int y, double[] result);
}
