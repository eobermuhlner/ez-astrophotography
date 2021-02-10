package ch.obermuhlner.astro;

import ch.obermuhlner.astro.image.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GradientRemover {

  private static final boolean DEBUG_GRADIENT = false;
  private static final boolean DEBUG_SHOW_FIX_POINTS = false;

  private static double[][] DEBUG_COLORS = {
      { 1, 0, 0 },
      { 0, 1, 0 },
      { 0, 0, 1 },
      { 1, 1, 0 },
      { 1, 0, 1 },
      { 0, 1, 1 },
  };

  private int autoFixPointsGridSize = 3;
  private int sampleRadius = 3;
  private double removalFactor = 1.0;
  private boolean adaptiveGradient = false;

  private final List<Point> fixPoints = new ArrayList<>();
  private final List<double[]> fixColors = new ArrayList<>();

  public void setRemovalFactor(double removalFactor) {
    this.removalFactor = removalFactor;
  }

  public void setFixPoints(List<Point> fixPoints, DoubleImage image, int sampleRadius) {
    List<double[]> fixColors = new ArrayList<>();

    for (Point fixPoint : fixPoints) {
      fixColors.add(getAverageColor(image, fixPoint.x, fixPoint.y, sampleRadius));
    }

    setFixPoints(fixPoints, fixColors);
  }

  public void setFixPoints(List<Point> fixPoints, List<double[]> fixColors) {
    this.fixPoints.clear();
    this.fixPoints.addAll(fixPoints);

    this.fixColors.clear();
    this.fixColors.addAll(fixColors);
  }

  public void removeGradient(DoubleImage input, DoubleImage gradient, DoubleImage output) {
    removeGradient(input, gradient, output, 0, 0);
  }

  public void removeGradient(DoubleImage input, DoubleImage gradient, DoubleImage output, int offsetX, int offsetY) {
    double[] distances = new double[fixPoints.size()];
    double[] factors = new double[fixPoints.size()];

    double[] gradientColor = new double[3];
    double[] inputColor = new double[3];
    double[] outputColor = new double[3];

    for (int y = 0; y < input.getHeight(); y++) {
      for (int x = 0; x < input.getWidth(); x++) {
        Point point = new Point(offsetX + x, offsetY + y);

        double maxDistance = 0;
        for (int i = 0; i < fixPoints.size(); i++) {
          Point gradientPoint = fixPoints.get(i);
          distances[i] = point.distance(gradientPoint);
          maxDistance = Math.max(maxDistance, distances[i]);
        }

        double totalFactor = 0;
        for (int i = 0; i < fixPoints.size(); i++) {
          double factor = 1.0 - distances[i] / maxDistance;
          factor = factor * factor * factor;
          factors[i] = factor;
          totalFactor += factor;
        }

        if (fixPoints.size() == 1) {
          factors[0] = 1;
          totalFactor = 1;
        }

        gradientColor[ColorModel.R] = 0;
        gradientColor[ColorModel.G] = 0;
        gradientColor[ColorModel.B] = 0;
        for (int i = 0; i < fixPoints.size(); i++) {
          double factor = factors[i] / totalFactor;
          double[] fixColor = fixColors.get(i);
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

  private double[] getAverageColor(DoubleImage image, int x, int y, int sampleRadius) {
    int n = 0;
    double r = 0;
    double g = 0;
    double b = 0;
    double[] rgb = new double[3];
    for (int sy = y-sampleRadius; sy <= y+sampleRadius; sy++) {
      for (int sx = x-sampleRadius; sx < x+sampleRadius; sx++) {
        if (ImageUtil.isInsideImage(image, sx, sy)) {
          image.getPixel(sx, sy, ColorModel.RGB, rgb);
          r += rgb[ColorModel.R];
          g += rgb[ColorModel.G];
          b += rgb[ColorModel.B];
          n++;
        }
      }
    }
    return new double[] { r/n, g/n, b/n };
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
