package ch.obermuhlner.astro.image;

public class RGBColor {
  public final double r;
  public final double g;
  public final double b;

  public final static RGBColor Red = new RGBColor(1.0, 0, 0);

  public static RGBColor fromIntRGB(int rgb) {
    return new RGBColor(
        ((rgb >> 16) & 0xff) / 255.0,
        ((rgb >> 8) & 0xff) / 255.0,
        (rgb & 0xff) / 255.0
    );
  }

  public static RGBColor fromHSV(HSVColor hsv) {
    return fromHSV(hsv.h, hsv.s, hsv.v);
  }

  private static RGBColor fromHSV(double h, double s, double v) {
    return RGBColor.fromIntRGB(java.awt.Color.HSBtoRGB((float) h, (float) s, (float) v));
  }

  public RGBColor(double r, double g, double b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public RGBColor plus(RGBColor other) {
    return new RGBColor(
        Math.min(1.0, r + other.r),
        Math.min(1.0, g + other.g),
        Math.min(1.0, b + other.b)
    );
  }

  public RGBColor minus(RGBColor other) {
    return new RGBColor(
        Math.max(0, r - other.r),
        Math.max(0, g - other.g),
        Math.max(0, b - other.b)
    );
  }

  public RGBColor multiply(double value) {
    return new RGBColor(
        r * value,
        g * value,
        b * value
    );
  }

  public RGBColor interpolate(RGBColor end, double weight) {
    if (weight <= 0.0) {
      return this;
    }
    if (weight >= 1.0) {
      return end;
    }
    return new RGBColor(
        r + (end.r - r) * weight,
        g + (end.g - g) * weight,
        b + (end.b - b) * weight);
  }

  public int toIntRGB() {
    int rr = Math.max(Math.min((int) (r * 256), 255), 0);
    int gg = Math.max(Math.min((int) (g * 256), 255), 0);
    int bb = Math.max(Math.min((int) (b * 256), 255), 0);

    return (rr * 0x100 + gg) * 0x100 + bb;
  }

  @Override
  public String toString() {
    return "RGB{" +
        "r=" + r +
        ", g=" + g +
        ", b=" + b +
        '}';
  }
}
