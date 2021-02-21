package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.gradient.Point;
import ch.obermuhlner.astro.gradient.points.AllPointsFinder;
import ch.obermuhlner.astro.gradient.points.PointsFinder;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.color.ColorModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradientInterpolationFilter implements Filter {

  private PointsFinder pointsFinder = new AllPointsFinder();
  private double interpolationPower = 3.0;
  private boolean adaptiveGradient = false;

  private final Map<Point, double[]> mapPointToColor = new HashMap<>();

  public GradientInterpolationFilter() {
  }

  public GradientInterpolationFilter(double interpolationPower) {
    this.interpolationPower = interpolationPower;
  }

  public void setPointsFinder(PointsFinder pointsFinder) {
    this.pointsFinder = pointsFinder;
    pointsFinder.setFixPoints(mapPointToColor.keySet());
  }

  public void setInterpolationPower(double interpolationPower) {
    this.interpolationPower = interpolationPower;
  }

  public double getInterpolationPower() {
    return interpolationPower;
  }

  public void setFixPoints(List<Point> fixPoints, DoubleImage image, int sampleRadius) {
    List<double[]> fixColors = new ArrayList<>();

    for (Point fixPoint : fixPoints) {
      //    if (color == null) {
//      color = new double[3];
//    }
//
//    int n = 0;
//    double sample0 = 0;
//    double sample1 = 0;
//    double sample2 = 0;
//    for (int sy = y-sampleRadius; sy <= y+sampleRadius; sy++) {
//      for (int sx = x-sampleRadius; sx < x+sampleRadius; sx++) {
//        if (image.isInside(sx, sy)) {
//          image.getPixel(sx, sy, colorModel, color);
//          sample0 += color[0];
//          sample1 += color[1];
//          sample2 += color[2];
//          n++;
//        }
//      }
//    }
//
//    color[0] = sample0 / n;
//    color[1] = sample1 / n;
//    color[2] = sample2 / n;
//    return color;
      fixColors.add(image.averagePixel(fixPoint.x - sampleRadius,
          fixPoint.y - sampleRadius,
          sampleRadius + sampleRadius + 1,
          sampleRadius + sampleRadius + 1,
          ColorModel.RGB,
          null
      ));
    }

    setFixPoints(fixPoints, fixColors);
  }

  public void setFixPoints(List<Point> fixPoints, List<double[]> fixColors) {
    mapPointToColor.clear();
    for (int i = 0; i < fixPoints.size(); i++) {
      mapPointToColor.put(fixPoints.get(i), fixColors.get(i));
    }

    pointsFinder.setFixPoints(fixPoints);
  }

  private List<Point> getRelevantFixPoints(Point point) {
    return pointsFinder.getRelevantFixPoints(point);
  }

  private List<double[]> getRelevantFixColors(List<Point> relevantFixPoints) {
    List<double[]> result = new ArrayList<>();

    for (Point relevantFixPoint : relevantFixPoints) {
      result.add(mapPointToColor.get(relevantFixPoint));
    }

    return result;
  }

  @Override
  public DoubleImage filter(DoubleImage source, DoubleImage target, int width, int height) {
    double[] sourceColor = new double[3];
    double[] gradientColor = new double[3];

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Point point = new Point(x, y);

        List<Point> relevantFixPoints = getRelevantFixPoints(point);
        List<double[]> relevantFixColors = getRelevantFixColors(relevantFixPoints);
        int n = relevantFixPoints.size();
        double[] distances = new double[n];
        double[] factors = new double[n];

        double totalDistance = 0;
        for (int i = 0; i < n; i++) {
          Point gradientPoint = relevantFixPoints.get(i);
          distances[i] = point.distance(gradientPoint);
          totalDistance += distances[i];
        }

        double totalFactor = 0;
        for (int i = 0; i < n; i++) {
          double factor = 1.0 - distances[i] / totalDistance;
          factor = Math.pow(factor, interpolationPower);
          factors[i] = factor;
          totalFactor += factor;
        }

        if (n == 1) {
          factors[0] = 1;
          totalFactor = 1;
        }

        gradientColor[ColorModel.RGB.R] = 0;
        gradientColor[ColorModel.RGB.G] = 0;
        gradientColor[ColorModel.RGB.B] = 0;
        for (int i = 0; i < n; i++) {
          double factor = factors[i] / totalFactor;
          double[] fixColor = relevantFixColors.get(i);
          gradientColor[ColorModel.RGB.R] += fixColor[ColorModel.RGB.R] * factor;
          gradientColor[ColorModel.RGB.G] += fixColor[ColorModel.RGB.G] * factor;
          gradientColor[ColorModel.RGB.B] += fixColor[ColorModel.RGB.B] * factor;
        }

        source.getPixel(x, y, ColorModel.RGB, sourceColor);

        if (adaptiveGradient) {
//          HSVColor imageHSV = HSVColor.fromRGB(inputColor);
//          HSVColor gradientHSV = HSVColor.fromRGB(gradientColor);
//          double v = (imageHSV.v + gradientHSV.v) / 2;
//          gradientHSV = new HSVColor(gradientHSV.h, gradientHSV.s, v);
//          gradientColor = RGBColor.fromHSV(gradientHSV);
        }

        target.setPixel(x, y, ColorModel.RGB, gradientColor);
      }
    }

    return target;
  }

  @Override
  public String toString() {
    return "Gradient{" +
        "pointsFinder=" + pointsFinder +
        ", interpolationPower=" + interpolationPower +
        ", adaptiveGradient=" + adaptiveGradient +
        ", mapPointToColor=" + mapPointToColor +
        '}';
  }
}
