package ch.obermuhlner.astro.gradient.points;

import ch.obermuhlner.astro.gradient.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AllPointsFinder implements PointsFinder {

  private final List<Point> points = new ArrayList<>();

  @Override
  public void setFixPoints(Collection<Point> points) {
    this.points.clear();
    this.points.addAll(points);
  }

  @Override
  public List<Point> getRelevantFixPoints(Point point) {
    return points;
  }
}
