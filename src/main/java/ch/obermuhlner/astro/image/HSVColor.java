package ch.obermuhlner.astro.image;

public class HSVColor {
  public final double h;
  public final double s;
  public final double v;

  public static HSVColor fromRGB(RGBColor rgb) {
    return fromRGB(rgb.r, rgb.g, rgb.b);
  }

  private static float[] hsbvals = new float[3];
  public static HSVColor fromRGB(int rgb) {
    java.awt.Color.RGBtoHSB(
        ((rgb >> 16) & 0xff),
        ((rgb >> 8) & 0xff),
        (rgb & 0xff),
        hsbvals);
    return new HSVColor(hsbvals[0], hsbvals[1], hsbvals[2]);
  }

  public static HSVColor fromRGB(double r, double g, double b) {
    double hue, saturation, brightness;

    double cmax = (r > g) ? r : g;
    if (b > cmax) cmax = b;
    double cmin = (r < g) ? r : g;
    if (b < cmin) cmin = b;

    brightness = cmax;
    if (cmax != 0)
      saturation = (cmax - cmin) / cmax;
    else
      saturation = 0;
    if (saturation == 0)
      hue = 0;
    else {
      double redc = ((cmax - r)) / ((cmax - cmin));
      double greenc = ((cmax - g)) / ((cmax - cmin));
      double bluec = ((cmax - b)) / ((cmax - cmin));
      if (r == cmax)
        hue = bluec - greenc;
      else if (g == cmax)
        hue = 2.0f + redc - bluec;
      else
        hue = 4.0f + greenc - redc;
      hue = hue / 6.0f;
      if (hue < 0)
        hue = hue + 1.0f;
    }

    return new HSVColor(hue, saturation, brightness);
  }

  public HSVColor(double h, double s, double v) {
    this.h = h;
    this.s = s;
    this.v = v;
  }

  @Override
  public String toString() {
    return "HSVColor{" +
        "h=" + h +
        ", s=" + s +
        ", v=" + v +
        '}';
  }
}
