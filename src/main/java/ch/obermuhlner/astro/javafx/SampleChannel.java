package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.image.color.ColorModel;

public enum SampleChannel {
  Red(ColorModel.RGB, 0),
  Green(ColorModel.RGB, 1),
  Blue(ColorModel.RGB, 2),
  Hue(ColorModel.HSV, 0),
  Saturation(ColorModel.HSV, 1),
  Brightness(ColorModel.HSV, 2);

  private final ColorModel colorModel;
  private final int sampleIndex;

  SampleChannel(ColorModel colorModel, int sampleIndex) {
    this.colorModel = colorModel;
    this.sampleIndex = sampleIndex;
  }

  public ColorModel getColorModel() {
    return colorModel;
  }

  public int getSampleIndex() {
    return sampleIndex;
  }
}
