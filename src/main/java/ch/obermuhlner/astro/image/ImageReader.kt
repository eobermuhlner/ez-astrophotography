package ch.obermuhlner.astro.image

import mil.nga.tiff.TiffReader
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

object ImageReader {
    @JvmOverloads
    @Throws(IOException::class)
    fun read(file: File, quality: ImageQuality = ImageQuality.High): DoubleImage {
        if (quality == ImageQuality.High) {
            try {
                return TiffDoubleImage(TiffReader.readTiff(file), true)
            } catch (ex: Exception) {
                // ignore
            }
        }
        return AwtBufferedDoubleImage(ImageIO.read(file))
    }
}