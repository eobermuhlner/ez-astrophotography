package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.gradient.GradientRemover;
import ch.obermuhlner.astro.image.DoubleImage;

public class GradientRemoverFilter implements Filter {

  private final GradientRemover gradientRemover;

  public GradientRemoverFilter(GradientRemover gradientRemover) {
    this.gradientRemover = gradientRemover;
  }

  @Override
  public void filter(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height) {
    // TODO make API compatible
    gradientRemover.removeGradient(source, null, target, sourceX, sourceY);
  }
}
