package ch.obermuhlner.astro.gradient;

import ch.obermuhlner.astro.gradient.points.AllPointsFinder;
import ch.obermuhlner.astro.gradient.points.PointsFinder;
import ch.obermuhlner.astro.image.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GradientRemover {

  private static double[][] DEBUG_COLORS = {
      { 1, 0, 0 },
      { 0, 1, 0 },
      { 0, 0, 1 },
      { 1, 1, 0 },
      { 1, 0, 1 },
      { 0, 1, 1 },
  };

  private PointsFinder pointsFinder = new AllPointsFinder();
  private double interpolationPower = 3.0;
  private double removalFactor = 1.0;
  private boolean adaptiveGradient = false;

  private final Map<Point, double[]> mapPointToColor = new HashMap<>();

  public void setPointsFinder(PointsFinder pointsFinder) {
    this.pointsFinder = pointsFinder;
    pointsFinder.setFixPoints(mapPointToColor.keySet());
  }

  public void setRemovalFactor(double removalFactor) {
    this.removalFactor = removalFactor;
  }

  public void setInterpolationPower(double interpolationPower) {
    this.interpolationPower = interpolationPower;
  }

  public void setFixPoints(List<Point> fixPoints, DoubleImage image, int sampleRadius) {
    List<double[]> fixColors = new ArrayList<>();

    for (Point fixPoint : fixPoints) {
      fixColors.add(ImageUtil.averagePixel(image, fixPoint.x, fixPoint.y, sampleRadius, ColorModel.RGB));
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

  public void removeGradient(DoubleImage input, DoubleImage gradient, DoubleImage output) {
    removeGradient(input, gradient, output, 0, 0);
  }

  public void removeGradient(DoubleImage input, DoubleImage gradient, DoubleImage output, int offsetX, int offsetY) {
    double[] gradientColor = new double[3];
    double[] inputColor = new double[3];
    double[] outputColor = new double[3];

    for (int y = 0; y < input.getHeight(); y++) {
      for (int x = 0; x < input.getWidth(); x++) {
        Point point = new Point(offsetX + x, offsetY + y);

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

        gradientColor[ColorModel.R] = 0;
        gradientColor[ColorModel.G] = 0;
        gradientColor[ColorModel.B] = 0;
        for (int i = 0; i < n; i++) {
          double factor = factors[i] / totalFactor;
          double[] fixColor = relevantFixColors.get(i);
          gradientColor[ColorModel.R] += fixColor[ColorModel.R] * factor;
          gradientColor[ColorModel.G] += fixColor[ColorModel.G] * factor;
          gradientColor[ColorModel.B] += fixColor[ColorModel.B] * factor;
        }

        input.getPixel(x, y, ColorModel.RGB, inputColor);

        double pixelRemovalFactor = removalFactor;
        gradientColor[ColorModel.R] = gradientColor[ColorModel.R] * pixelRemovalFactor;
        gradientColor[ColorModel.G] = gradientColor[ColorModel.G] * pixelRemovalFactor;
        gradientColor[ColorModel.B] = gradientColor[ColorModel.B] * pixelRemovalFactor;
        if (adaptiveGradient) {
//          HSVColor imageHSV = HSVColor.fromRGB(inputColor);
//          HSVColor gradientHSV = HSVColor.fromRGB(gradientColor);
//          double v = (imageHSV.v + gradientHSV.v) / 2;
//          gradientHSV = new HSVColor(gradientHSV.h, gradientHSV.s, v);
//          gradientColor = RGBColor.fromHSV(gradientHSV);
        }

        if (gradient != null) {
          gradient.setPixel(x, y, ColorModel.RGB, gradientColor);
        }

        outputColor[ColorModel.R] = inputColor[ColorModel.R] - gradientColor[ColorModel.R];
        outputColor[ColorModel.G] = inputColor[ColorModel.G] - gradientColor[ColorModel.G];
        outputColor[ColorModel.B] = inputColor[ColorModel.B] - gradientColor[ColorModel.B];

        if (output != null) {
          output.setPixel(x, y, ColorModel.RGB, outputColor);
        }
      }
    }
  }

  private double[] getDebugColor(int i) {
    if (i < DEBUG_COLORS.length) {
      return DEBUG_COLORS[i];
    }
    Random random = new Random(i);
    return new double[] { random.nextDouble(), random.nextDouble(), random.nextDouble() };
  }

  private double smoothstep(double edge0, double edge1, double x) {
    double t = Math.min(Math.max((x - edge0) / (edge1 - edge0), 0.0), 1.0);
    return t * t * (3.0 - 2.0 * t);
  }

  private boolean isFixPointNeighbour(int x, int y, Point[] points) {
    for (Point point : points) {
      if ((point.x + 1 == x || point.x - 1 == x) && (point.y + 1 == y || point.y - 1 == y)) {
        return true;
      }
    }
    return false;
  }
}
