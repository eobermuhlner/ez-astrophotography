package ch.obermuhlner.astro.gradient.points;

import ch.obermuhlner.astro.gradient.Point;
import io.github.jdiemke.triangulation.DelaunayTriangulator;
import io.github.jdiemke.triangulation.NotEnoughPointsException;
import io.github.jdiemke.triangulation.Triangle2D;
import io.github.jdiemke.triangulation.Vector2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VoronoiPointsFinder implements PointsFinder {

  private final List<Point> points = new ArrayList<>();

  private List<Vector2D> delaunayPoints;
  private List<Triangle2D> delaunayTriangles;
  private Map<Vector2D, Point> delaunayMapToPoints;

  @Override
  public void setFixPoints(Collection<Point> points) {
    this.points.clear();
    this.points.addAll(points);

    triangulate(this.points);
  }

  private void triangulate(List<Point> points) {
    if (points.size() < 3) {
      delaunayPoints = null;
      delaunayTriangles = null;
      return;
    }

    delaunayMapToPoints = new HashMap<>();
    List<Vector2D> vectors = new ArrayList<>();
    for (Point point : points) {
      Vector2D v = new Vector2D(point.x, point.y);
      delaunayMapToPoints.put(v, point);
      vectors.add(v);
    }
    DelaunayTriangulator triangulator = new DelaunayTriangulator(vectors);
    try {
      triangulator.triangulate();
    }
    catch (NotEnoughPointsException e) {
      throw new RuntimeException(e);
    }

    delaunayPoints = triangulator.getPointSet();
    delaunayTriangles = triangulator.getTriangles();
  }
  @Override
  public List<Point> getRelevantFixPoints(Point point) {
    Set<Vector2D> delaunayPolygon = new HashSet<>();

    if (points.size() <= 3) {
      return points;
    }

    Vector2D vector = new Vector2D(point.x, point.y);
    Vector2D closestDelaunayPoint = null;
    double closestDistance = Double.MAX_VALUE;
    for (Vector2D delaunayPoint : delaunayPoints) {
      double distance = vector.sub(delaunayPoint).mag();
      if (closestDelaunayPoint == null || distance < closestDistance) {
        closestDelaunayPoint = delaunayPoint;
        closestDistance = distance;
      }
    }

    for (Triangle2D triangle : delaunayTriangles) {
      if (triangle.hasVertex(closestDelaunayPoint)) {
        delaunayPolygon.add(triangle.a);
        delaunayPolygon.add(triangle.b);
        delaunayPolygon.add(triangle.c);
      }
    }

    List<Point> result = new ArrayList<>();
    for (Vector2D vector2D : delaunayPolygon) {
      result.add(delaunayMapToPoints.get(vector2D));
    }
    return result;
  }
}
