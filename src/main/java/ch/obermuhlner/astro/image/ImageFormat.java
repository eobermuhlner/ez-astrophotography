package ch.obermuhlner.astro.image;

enum ImageFormat {
  TIF("tif", "tiff"),
  PNG("png"),
  JPG("jpg", "jpeg");

  private final String[] extensions;

  ImageFormat(String... extensions) {
    this.extensions = extensions;
  }

  public String[] getExtensions() {
    return extensions;
  }
}
