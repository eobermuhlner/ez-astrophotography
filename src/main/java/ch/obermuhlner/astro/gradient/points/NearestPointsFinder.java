package ch.obermuhlner.astro.gradient.points;

import ch.obermuhlner.astro.gradient.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class NearestPointsFinder implements PointsFinder {

  private final int n;
  private final List<Point> points = new ArrayList<>();

  public NearestPointsFinder(int n) {
    this.n = n;
  }

  @Override
  public void setFixPoints(Collection<Point> points) {
    this.points.clear();
    this.points.addAll(points);
  }

  @Override
  public List<Point> getRelevantFixPoints(Point point) {
    points.sort(Comparator.comparingDouble(p -> point.distanceSquare(p)));

    List result = new ArrayList(n);
    for (int i = 0; i < Math.min(n, points.size()); i++) {
      result.add(points.get(i));
    }
    return result;
  }
}
