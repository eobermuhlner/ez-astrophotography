package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.gradient.points.AllPointsFinder;
import ch.obermuhlner.astro.gradient.points.NearestPointsFinder;
import ch.obermuhlner.astro.gradient.points.PointsFinder;
import ch.obermuhlner.astro.gradient.points.VoronoiPointsFinder;

public enum PointFinderStrategy {
  All(new AllPointsFinder()),
  Nearest3(new NearestPointsFinder(3)),
  Nearest5(new NearestPointsFinder(5)),
  Voronoi(new VoronoiPointsFinder());

  private final PointsFinder pointsFinder;

  private PointFinderStrategy(PointsFinder pointsFinder) {
    this.pointsFinder = pointsFinder;
  }

  public PointsFinder getPointsFinder() {
    return pointsFinder;
  }
}
