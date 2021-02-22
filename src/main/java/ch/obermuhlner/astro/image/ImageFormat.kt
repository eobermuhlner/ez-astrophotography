package ch.obermuhlner.astro.image

enum class ImageFormat(vararg extensions: String) {
    TIF("tif", "tiff"),
    PNG("png"),
    JPG("jpg", "jpeg");

    val extensions: Array<String> = extensions as Array<String>

}