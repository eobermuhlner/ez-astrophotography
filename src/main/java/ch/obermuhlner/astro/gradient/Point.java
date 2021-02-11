package ch.obermuhlner.astro.gradient;

import java.util.Objects;

public class Point {
  public final int x;
  public final int y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public double distanceSquare(Point other) {
    double dx = other.x - x;
    double dy = other.y - y;

    return dx*dx + dy*dy;
  }

  public double distance(Point other) {
    return Math.sqrt(distanceSquare(other));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Point point = (Point) o;
    return x == point.x &&
        y == point.y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public String toString() {
    return "Point{" +
        "x=" + x +
        ", y=" + y +
        '}';
  }
}
