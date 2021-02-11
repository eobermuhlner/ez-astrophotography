package ch.obermuhlner.astro.gradient;

public class Point {
  public final int x;
  public final int y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public double distance(Point other) {
    double dx = other.x - x;
    double dy = other.y - y;

    return Math.sqrt(dx*dx + dy*dy);
  }

  @Override
  public String toString() {
    return "Point{" +
        "x=" + x +
        ", y=" + y +
        '}';
  }
}
