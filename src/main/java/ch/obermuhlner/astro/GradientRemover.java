package ch.obermuhlner.astro;

import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.HSVColor;
import ch.obermuhlner.astro.image.ImageCreator;
import ch.obermuhlner.astro.image.ImageQuality;
import ch.obermuhlner.astro.image.ImageReader;
import ch.obermuhlner.astro.image.ImageWriter;
import ch.obermuhlner.astro.image.RGBColor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GradientRemover {

  private static final boolean DEBUG_GRADIENT = false;
  private static final boolean DEBUG_SHOW_FIX_POINTS = false;

  private static RGBColor[] DEBUG_COLORS = {
      RGBColor.fromIntRGB(0xff0000),
      RGBColor.fromIntRGB(0x00ff00),
      RGBColor.fromIntRGB(0x0000ff),
      RGBColor.fromIntRGB(0xffff00),
      RGBColor.fromIntRGB(0xff00ff),
      RGBColor.fromIntRGB(0x00ffff),
  };

  private int autoFixPointsGridSize = 3;
  private int sampleRadius = 3;
  private double removalFactor = 1.0;
  private boolean adaptiveGradient = false;

  private final List<Point> fixPoints = new ArrayList<>();
  private final List<RGBColor> fixColors = new ArrayList<>();

  public void gradient(String filename) {
    try {
      DoubleImage image = ImageReader.read(new File(filename));

      int n = autoFixPointsGridSize;
      int gridWidth = image.getWidth() / n;
      int gridHeight = image.getHeight() / n;

      Point[] points = new Point[n*n];
      RGBColor[] pointColors = new RGBColor[n*n];

      for (int gridY = 0; gridY < n; gridY++) {
        for (int gridX = 0; gridX < n; gridX++) {
          double r = 0;
          double g = 0;
          double b = 0;
          int count = 0;
//          int minX = 0;
//          int minY = 0;
//          double minBrightness = 1.0;
          for (int smallY = 0; smallY < gridHeight; smallY++) {
            for (int smallX = 0; smallX < gridWidth; smallX++) {
              int x = gridY * gridWidth + smallY;
              int y = gridX * gridHeight + smallX;
              if (isInsideImage(image, x, y)) {
                RGBColor rgb = image.getPixel(x, y);
                r += rgb.r;
                g += rgb.g;
                b += rgb.b;
                count++;
//                double brightness = HSVColor.fromRGB(rgb).v;
//                if (brightness < minBrightness) {
//                  minX = x;
//                  minY = y;
//                  minBrightness = brightness;
//                }
              }
            }
          }
          int index = gridY * n + gridX;
          points[index] = new Point(gridX * gridWidth + gridWidth/2, gridY * gridHeight + gridHeight/2);
          if (DEBUG_GRADIENT) {
            pointColors[index] = getDebugColor(index);
          } else {
            pointColors[index] = new RGBColor(r/count, g/count, b/count);
          }
//          points[gridY * n + gridX] = new Point(minX, minY);
        }
      }

      gradient(filename, image, points, pointColors);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setFixPoints(List<Point> fixPoints, DoubleImage image, int sampleRadius) {
    List<RGBColor> fixColors = new ArrayList<>();

    for (Point fixPoint : fixPoints) {
      fixColors.add(getAverageColor(image, fixPoint.x, fixPoint.y, sampleRadius));
    }

    setFixPoints(fixPoints, fixColors);
  }

  public void setFixPoints(List<Point> fixPoints, List<RGBColor> fixColors) {
    this.fixPoints.clear();
    this.fixPoints.addAll(fixPoints);

    this.fixColors.clear();
    this.fixColors.addAll(fixColors);
  }

  public void removeGradient(DoubleImage input, DoubleImage gradient, DoubleImage output, int offsetX, int offsetY) {
    double[] distances = new double[fixPoints.size()];
    double[] factors = new double[fixPoints.size()];

    for (int y = 0; y < input.getHeight(); y++) {
      for (int x = 0; x < input.getWidth(); x++) {
        Point point = new Point(offsetX + x, offsetY + y);
        RGBColor gradientColor;

        if (true) {
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

          gradientColor = new RGBColor(0, 0, 0);
          for (int i = 0; i < fixPoints.size(); i++) {
            double factor = factors[i] / totalFactor;
            gradientColor = gradientColor.plus(fixColors.get(i).multiply(factor));
          }
        } else {
          gradientColor = fixColors.get(0);
          double lastDistance = fixPoints.get(0).distance(point);
          for (int i = 1; i < fixPoints.size(); i++) {
            double thisDistance = fixPoints.get(i).distance(point);
            double thisFactor = thisDistance / (lastDistance + thisDistance);
            thisFactor = smoothstep(0, 1, thisFactor);
            gradientColor = fixColors.get(i).interpolate(gradientColor, thisFactor);
            lastDistance = thisDistance;
          }
        }

        RGBColor inputColor = input.getPixel(x, y);

        double pixelRemovalFactor = removalFactor;
        gradientColor = gradientColor.multiply(pixelRemovalFactor);
        if (adaptiveGradient) {
          HSVColor imageHSV = HSVColor.fromRGB(inputColor);
          HSVColor gradientHSV = HSVColor.fromRGB(gradientColor);
          double v = (imageHSV.v + gradientHSV.v) / 2;
          gradientHSV = new HSVColor(gradientHSV.h, gradientHSV.s, v);
          gradientColor = RGBColor.fromHSV(gradientHSV);
        }

        if (gradient != null) {
          gradient.setPixel(x, y, gradientColor);
        }

        RGBColor outputColor = inputColor.minus(gradientColor);

        if (output != null) {
          output.setPixel(x, y, outputColor);
        }
      }
    }
  }

  public void gradient(String filename, Point[] points) {
    try {
      DoubleImage image = ImageReader.read(new File(filename));
      gradient(filename, image, points);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void gradient(String filename, DoubleImage image, Point[] points) {
    RGBColor[] gradientColors = new RGBColor[points.length];

    for (int i = 0; i < points.length; i++) {
      Point point = points[i];
      if (DEBUG_GRADIENT) {
        gradientColors[i] = getDebugColor(i);
      } else {
        gradientColors[i] = getAverageColor(image, point.x, point.y, sampleRadius);
      }
      System.out.println(point + " : " + gradientColors[i]);
    }

    gradient(filename, image, points, gradientColors);
  }

  private RGBColor getDebugColor(int i) {
    if (i < DEBUG_COLORS.length) {
      return DEBUG_COLORS[i];
    }
    Random random = new Random(i);
    return new RGBColor(random.nextDouble(), random.nextDouble(), random.nextDouble());
  }

  private RGBColor getAverageColor(DoubleImage image, int x, int y, int sampleRadius) {
    int n = 0;
    double r = 0;
    double g = 0;
    double b = 0;
    for (int sy = y-sampleRadius; sy <= y+sampleRadius; sy++) {
      for (int sx = x-sampleRadius; sx < x+sampleRadius; sx++) {
        if (isInsideImage(image, sx, sy)) {
          RGBColor color = image.getPixel(sx, sy);
          r += color.r;
          g += color.g;
          b += color.b;
          n++;
        }
      }
    }
    return new RGBColor(r / n, g / n, b / n);
  }

  private boolean isInsideImage(DoubleImage image, int x, int y) {
    return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
  }

  private void gradient(String filename, DoubleImage image, Point[] points, RGBColor[] gradientColors) {
    double[] distances = new double[points.length];
    double[] factors = new double[points.length];

    try {
      DoubleImage gradient = ImageCreator.create(image.getWidth(), image.getHeight(), ImageQuality.High);
      DoubleImage output = ImageCreator.create(image.getWidth(), image.getHeight(), ImageQuality.High);

      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          Point point = new Point(x, y);
          RGBColor gradientColor;

          if (true) {
            double maxDistance = 0;
            for (int i = 0; i < points.length; i++) {
              Point gradientPoint = points[i];
              distances[i] = point.distance(gradientPoint);
              maxDistance = Math.max(maxDistance, distances[i]);
            }

            double totalFactor = 0;
            for (int i = 0; i < points.length; i++) {
              double factor = 1.0 - distances[i] / maxDistance;
              factor = factor * factor * factor;
              factors[i] = factor;
              totalFactor += factor;
            }

            gradientColor = new RGBColor(0, 0, 0);
            for (int i = 0; i < points.length; i++) {
              double factor = factors[i] / totalFactor;
              gradientColor = gradientColor.plus(gradientColors[i].multiply(factor));
            }
          } else {
            gradientColor = gradientColors[0];
            double lastDistance = points[0].distance(point);
            for (int i = 1; i < points.length; i++) {
              double thisDistance = points[i].distance(point);
              double thisFactor = thisDistance / (lastDistance + thisDistance);
              thisFactor = smoothstep(0, 1, thisFactor);
              gradientColor = gradientColors[i].interpolate(gradientColor, thisFactor);
              lastDistance = thisDistance;
            }
          }

          RGBColor imageColor = image.getPixel(x, y);

          double pixelRemovalFactor = removalFactor;
          gradientColor = gradientColor.multiply(pixelRemovalFactor);
          if (adaptiveGradient) {
            HSVColor imageHSV = HSVColor.fromRGB(imageColor);
            HSVColor gradientHSV = HSVColor.fromRGB(gradientColor);
            double v = (imageHSV.v + gradientHSV.v) / 2;
            gradientHSV = new HSVColor(gradientHSV.h, gradientHSV.s, v);
            gradientColor = RGBColor.fromHSV(gradientHSV);
          }

          if (DEBUG_SHOW_FIX_POINTS && isFixPointNeighbour(x, y, points)) {
            gradient.setPixel(x, y, RGBColor.Red);
          } else {
            gradient.setPixel(x, y, gradientColor);
          }

          RGBColor outputColor = imageColor.minus(gradientColor);

          if (DEBUG_SHOW_FIX_POINTS && isFixPointNeighbour(x, y, points)) {
            output.setPixel(x, y, RGBColor.Red);
          } else {
            output.setPixel(x, y, outputColor);
          }
        }
      }

      ImageWriter.write(gradient, new File(filename + "_gradient.tif"));
      ImageWriter.write(output, new File(filename + "_output.tif"));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
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

  // Autosave001.tif 262,250 3071,291 1686,1403 333,3489 2937,3421 1716,1880 1723,3485 3201,2177 68,2103 1800,2040 3213,3692
  public static void main(String[] args) {
    Point[] points = new Point[args.length - 1];
    for (int i = 1; i < args.length; i++) {
      String[] split = args[i].split(",");
      points[i - 1] = new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    GradientRemover gradient = new GradientRemover();
    gradient.gradient(args[0]);
    //gradient.gradient(args[0], points);
  }
}
