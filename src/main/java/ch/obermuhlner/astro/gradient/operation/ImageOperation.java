package ch.obermuhlner.astro.gradient.operation;

import ch.obermuhlner.astro.image.ArrayDoubleImage;
import ch.obermuhlner.astro.image.DoubleImage;

public interface ImageOperation {
  DoubleImage operation(DoubleImage image1, DoubleImage image2, DoubleImage result);

  default DoubleImage operation(DoubleImage image1, DoubleImage image2) {
    return operation(image1, image2, new ArrayDoubleImage(image1.getWidth(), image1.getHeight(), image1.getColorModel()));
  }

}
