package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.ArrayDoubleImage;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.SubDoubleImage;

public interface Filter {
  DoubleImage filter(DoubleImage source, DoubleImage target, int width, int height);

  default DoubleImage filter(DoubleImage source, DoubleImage target) {
    int width = Math.min(source.getWidth(), target.getWidth());
    int height = Math.min(source.getHeight(), target.getHeight());
    return filter(source, target, width, height);
  }

  default DoubleImage filter(DoubleImage source) {
    ArrayDoubleImage target = new ArrayDoubleImage(source.getWidth(), source.getHeight(), source.getColorModel());
    return filter(source, target);
  }

  default DoubleImage filter(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height) {
    SubDoubleImage subSource = new SubDoubleImage(source, sourceX, sourceY, width, height);
    SubDoubleImage subTarget = new SubDoubleImage(target, targetX, targetY, width, height);

    return filter(subSource, subTarget);
  }

}
