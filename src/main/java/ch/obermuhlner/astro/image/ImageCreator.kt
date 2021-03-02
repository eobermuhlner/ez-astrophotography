package ch.obermuhlner.astro.image

import mil.nga.tiff.FieldType
import mil.nga.tiff.FileDirectory
import mil.nga.tiff.Rasters
import mil.nga.tiff.TIFFImage
import mil.nga.tiff.util.TiffConstants
import java.awt.image.BufferedImage
import java.util.*

object ImageCreator {
    fun create(width: Int, height: Int, quality: ImageQuality): DoubleImage {
        return if (quality == ImageQuality.High) {
            createTiff(width, height)
        } else AwtBufferedDoubleImage(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB))
    }

    fun createTiff(width: Int, height: Int): DoubleImage {
        val samplesPerPixel = 3
        val fieldType = FieldType.FLOAT
        val bitsPerSample = fieldType.bits
        val rasters = Rasters(width, height, samplesPerPixel, fieldType)
        val rowsPerStrip = rasters.calculateRowsPerStrip(TiffConstants.PLANAR_CONFIGURATION_CHUNKY)
        val directory = FileDirectory()
        directory.setImageWidth(width)
        directory.setImageHeight(height)
        directory.bitsPerSample = listOf(bitsPerSample, bitsPerSample, bitsPerSample)
        directory.compression = TiffConstants.COMPRESSION_NO
        directory.photometricInterpretation = TiffConstants.PHOTOMETRIC_INTERPRETATION_RGB
        directory.samplesPerPixel = samplesPerPixel
        directory.setRowsPerStrip(rowsPerStrip)
        directory.planarConfiguration = TiffConstants.PLANAR_CONFIGURATION_CHUNKY
        directory.setSampleFormat(TiffConstants.SAMPLE_FORMAT_FLOAT)
        directory.writeRasters = rasters
        val tiffImage = TIFFImage()
        tiffImage.add(directory)
        return TiffDoubleImage(tiffImage, false)
    }
}