package ch.obermuhlner.astro.gradient.points;

import ch.obermuhlner.astro.gradient.Point;

import java.util.Collection;
import java.util.List;

public interface PointsFinder {
  void setFixPoints(Collection<Point> points);
  List<Point> getRelevantFixPoints(Point point);
}
