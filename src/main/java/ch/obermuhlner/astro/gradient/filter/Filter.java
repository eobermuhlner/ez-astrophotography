package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.SubDoubleImage;

public interface Filter {
  void filter(DoubleImage source, DoubleImage target, int width, int height);

  default void filter(DoubleImage source, DoubleImage target) {
    int width = Math.min(source.getWidth(), target.getWidth());
    int height = Math.min(source.getHeight(), target.getHeight());
    filter(source, target, width, height);
  }

  default void filter(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height) {
    SubDoubleImage subSource = new SubDoubleImage(source, sourceX, sourceY, width, height);
    SubDoubleImage subTarget = new SubDoubleImage(target, targetX, targetY, width, height);

    filter(subSource, subTarget);
  }

}
