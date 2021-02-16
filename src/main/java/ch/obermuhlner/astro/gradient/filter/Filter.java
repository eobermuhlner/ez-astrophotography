package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.DoubleImage;

public interface Filter {
  void filter(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height);

  default void filter(DoubleImage source, DoubleImage target) {
    int width = Math.min(source.getWidth(), target.getWidth());
    int height = Math.min(source.getHeight(), target.getHeight());
    filter(source, 0, 0, target, 0, 0, width, height);
  }
}
