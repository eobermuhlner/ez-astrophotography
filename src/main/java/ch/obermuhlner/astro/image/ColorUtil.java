package ch.obermuhlner.astro.image;

public class ColorUtil {
  public static int toIntRGB(double[] rgb) {
    int rr = Math.max(Math.min((int) (rgb[ColorModel.R] * 256), 255), 0);
    int gg = Math.max(Math.min((int) (rgb[ColorModel.G] * 256), 255), 0);
    int bb = Math.max(Math.min((int) (rgb[ColorModel.B] * 256), 255), 0);

    return (rr * 0x100 + gg) * 0x100 + bb;
  }

  public static double[] convertRGBtoHSV(double r, double g, double b, double[] hsv) {
    double hue, saturation, brightness;
    double cmax = (r > g) ? r : g;
    if (b > cmax) cmax = b;
    double cmin = (r < g) ? r : g;
    if (b < cmin) cmin = b;

    brightness = cmax;
    if (cmax != 0)
      saturation = (double) (cmax - cmin) / cmax;
    else
      saturation = 0;

    if (saturation == 0) {
      hue = 0;
    } else {
      double redc = (cmax - r) / (cmax - cmin);
      double greenc = (cmax - g) / (cmax - cmin);
      double bluec = (cmax - b) / (cmax - cmin);
      if (r == cmax)
        hue = bluec - greenc;
      else if (g == cmax)
        hue = 2.0 + redc - bluec;
      else
        hue = 4.0 + greenc - redc;
      hue = hue / 6.0;
      if (hue < 0)
        hue = hue + 1.0;
    }

    if (hsv == null) {
      hsv = new double[3];
    }
    hsv[0] = hue * 360;
    hsv[1] = saturation;
    hsv[2] = brightness;
    return hsv;
  }

  public static double[] convertHSVToRGB(double hue, double saturation, double value, double[] rgb) {
    // normalize the hue
    double normalizedHue = ((hue % 360) + 360) % 360;
    hue = normalizedHue/360;

    double r = 0, g = 0, b = 0;
    if (saturation == 0) {
      r = g = b = value;
    } else {
      double h = (hue - Math.floor(hue)) * 6.0;
      double f = h - java.lang.Math.floor(h);
      double p = value * (1.0 - saturation);
      double q = value * (1.0 - saturation * f);
      double t = value * (1.0 - (saturation * (1.0 - f)));
      switch ((int) h) {
        case 0:
          r = value;
          g = t;
          b = p;
          break;
        case 1:
          r = q;
          g = value;
          b = p;
          break;
        case 2:
          r = p;
          g = value;
          b = t;
          break;
        case 3:
          r = p;
          g = q;
          b = value;
          break;
        case 4:
          r = t;
          g = p;
          b = value;
          break;
        case 5:
          r = value;
          g = p;
          b = q;
          break;
      }
    }

    if (rgb == null) {
      rgb = new double[3];
    }
    rgb[ColorModel.R] = r;
    rgb[ColorModel.G] = g;
    rgb[ColorModel.B] = b;
    return rgb;
  }
}
