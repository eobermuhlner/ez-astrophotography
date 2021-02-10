package ch.obermuhlner.astro.image;

public class ColorUtil {

  public static int toIntRGB(double[] rgb) {
    int rr = Math.max(Math.min((int) (rgb[ColorModel.R] * 256), 255), 0);
    int gg = Math.max(Math.min((int) (rgb[ColorModel.G] * 256), 255), 0);
    int bb = Math.max(Math.min((int) (rgb[ColorModel.B] * 256), 255), 0);

    return (rr * 0x100 + gg) * 0x100 + bb;
  }

  public static double[] convertRGBtoHSV(double[] rgb, double[] hsv) {
    return convertRGBtoHSV(rgb, hsv, 0, 0);
  }

  public static double[] convertRGBtoHSV(double[] rgb, double[] hsv, int rgbOffset, int hsvOffset) {
    return convertRGBtoHSV(rgb[rgbOffset + ColorModel.R], rgb[rgbOffset + ColorModel.G], rgb[rgbOffset + ColorModel.B], hsv, hsvOffset);
  }

  public static double[] convertRGBtoHSV(double r, double g, double b, double[] hsv) {
    return convertRGBtoHSV(r, g, b, hsv, 0);
  }

  public static double[] convertRGBtoHSV(double r, double g, double b, double[] hsv, int hsvOffset) {
    double h, s, v;
    double cmax = (r > g) ? r : g;
    if (b > cmax) cmax = b;
    double cmin = (r < g) ? r : g;
    if (b < cmin) cmin = b;

    v = cmax;
    if (cmax != 0)
      s = (double) (cmax - cmin) / cmax;
    else
      s = 0;

    if (s == 0) {
      h = 0;
    } else {
      double redc = (cmax - r) / (cmax - cmin);
      double greenc = (cmax - g) / (cmax - cmin);
      double bluec = (cmax - b) / (cmax - cmin);
      if (r == cmax)
        h = bluec - greenc;
      else if (g == cmax)
        h = 2.0 + redc - bluec;
      else
        h = 4.0 + greenc - redc;
      h = h / 6.0;
      if (h < 0)
        h = h + 1.0;
    }

    if (hsv == null) {
      hsv = new double[3];
    }
    hsv[hsvOffset + ColorModel.H] = h * 360;
    hsv[hsvOffset + ColorModel.S] = s;
    hsv[hsvOffset + ColorModel.V] = v;
    return hsv;
  }

  public static double[] convertHSVtoRGB(double[] hsv, double[] rgb) {
    return convertHSVtoRGB(hsv, rgb, 0, 0);
  }

  public static double[] convertHSVtoRGB(double[] hsv, double[] rgb, int hsvOffset, int rgbOffset) {
    return convertHSVtoRGB(hsv[hsvOffset + ColorModel.H], hsv[hsvOffset + ColorModel.S], hsv[hsvOffset + ColorModel.V], rgb, rgbOffset);
  }

  public static double[] convertHSVtoRGB(double h, double s, double v, double[] rgb) {
    return convertHSVtoRGB(h, s, v, rgb, 0);
  }

  public static double[] convertHSVtoRGB(double h, double s, double v, double[] rgb, int rgbOffset) {
    double normalizedHue = ((h % 360) + 360) % 360;
    h = normalizedHue/360;

    double r = 0, g = 0, b = 0;
    if (s == 0) {
      r = g = b = v;
    } else {
      double hh = (h - Math.floor(h)) * 6.0;
      double f = hh - java.lang.Math.floor(hh);
      double p = v * (1.0 - s);
      double q = v * (1.0 - s * f);
      double t = v * (1.0 - (s * (1.0 - f)));
      switch ((int) hh) {
        case 0:
          r = v;
          g = t;
          b = p;
          break;
        case 1:
          r = q;
          g = v;
          b = p;
          break;
        case 2:
          r = p;
          g = v;
          b = t;
          break;
        case 3:
          r = p;
          g = q;
          b = v;
          break;
        case 4:
          r = t;
          g = p;
          b = v;
          break;
        case 5:
          r = v;
          g = p;
          b = q;
          break;
      }
    }

    if (rgb == null) {
      rgb = new double[3];
    }
    rgb[rgbOffset + ColorModel.R] = r;
    rgb[rgbOffset + ColorModel.G] = g;
    rgb[rgbOffset + ColorModel.B] = b;
    return rgb;
  }

  public static double sampleDistance(double[] sample1, double[] sample2, ColorModel colorModel, int sampleIndex, boolean normalize) {
    double delta = sample1[sampleIndex] - sample2[sampleIndex];
    if (colorModel == ColorModel.HSV && sampleIndex == ColorModel.H) {
      if (delta > 180) {
        delta -= 180;
      } else if (delta < -180) {
        delta += 180;
      }

      if (normalize) {
        delta = delta / 180;
      }
    }

    return delta;
  }
}
