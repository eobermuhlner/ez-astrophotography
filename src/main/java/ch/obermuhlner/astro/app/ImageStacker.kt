package ch.obermuhlner.astro.app

import ch.obermuhlner.astro.gradient.align.SimpleImageAligner
import ch.obermuhlner.astro.image.*
import ch.obermuhlner.astro.image.color.ColorModel
import com.xenomachina.argparser.ArgParser
import java.io.File

object ImageStacker {
    @JvmStatic
    fun main(args: Array<String>) {
        //val parser = ArgParser(args)

        val parser = ArgParser(arrayOf(
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2528.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2529.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2530.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2531.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2532.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2533.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2534.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2535.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2536.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2537.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2538.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2539.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2540.TIF",
            "C:/Users/EricObermuhlner/Pictures/Astrophotography/2021-03-29 M42/Moon/TIFF16/IMG_2541.TIF",
        ))

//        val parser = ArgParser(arrayOf(
//            "images/stack/IMG_6800.TIF",
//            "images/stack/IMG_6801.TIF",
//            "images/stack/IMG_6802.TIF",
//            "images/stack/IMG_6803.TIF",
//            "images/stack/IMG_6804.TIF"))

//        val parser = ArgParser(arrayOf(
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6800.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6801.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6802.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6803.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6804.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6805.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6806.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6807.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6808.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6809.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6810.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6811.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6812.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6813.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6814.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6815.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6816.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6817.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6818.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6819.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6820.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6821.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6822.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6823.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6824.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6825.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6826.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6827.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6828.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6829.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6830.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6831.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6832.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6833.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6834.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6835.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6836.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6837.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6838.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6839.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6840.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6841.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6842.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6843.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6844.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6845.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6846.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6847.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6848.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6849.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6850.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6851.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6852.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6853.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6854.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6855.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6856.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6857.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6858.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6859.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6860.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6861.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6862.TIF",
//            "D:/Photos/Astro/2021-02-25 M42/60s_ISO800_F6.3_400mm/TIFF16/IMG_6863.TIF"
//        ))

        val sources by parser.positionalList(
            help = "source filename")

        println("Loading base ${sources[0]}")
        val baseImage = ImageReader.read(File(sources[0]), ImageQuality.High).copyImage()

        val aligner = SimpleImageAligner(200)

        val errorThreshold = 0.2 //2.0E-3
        var countAddedImages = 0

        val output = ArrayDoubleImage(baseImage.width, baseImage.height, ColorModel.RGB)
        for (source in sources) {
            val sourceFile = File(source)

            val image = measureElapsed("Loaded $source") {
                if(source == sources[0]) {
                    baseImage
                } else {
                    ImageReader.read(sourceFile, ImageQuality.High).copyImage()
                }
            }
            val alignment = measureElapsed("Aligning with base") {
                aligner.align(baseImage, image, maxOffset=500)
            }
            println("Aligned ${alignment.x},${alignment.y} error=${alignment.error}")

            if (alignment.error <= errorThreshold) {
                measureElapsed("Adding image to output") {
                    addPixels(output, image.croppedImage(alignment.x, alignment.y, baseImage.width, baseImage.height))
                }
                countAddedImages++

                val outputFile = File(sourceFile.parent, "stacked_${countAddedImages}_${sourceFile.name}")
                writeAverageImage(output, countAddedImages, outputFile)
            } else {
                println("Skipping image because error > $errorThreshold")
            }
            println()
        }

        println("Total $countAddedImages images added")

        println("Finished")
    }

    private fun writeAverageImage(image: DoubleImage, count: Int, file: File) {
        measureElapsed("Saved output $file") {
            val tiffImage = ImageCreator.createTiff(image.width, image.height)

            val color = DoubleArray(3)
            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    image.getPixel(x, y, ColorModel.RGB, color)
                    color[0] /= count.toDouble()
                    color[1] /= count.toDouble()
                    color[2] /= count.toDouble()
                    tiffImage.setPixel(x, y, ColorModel.RGB, color)
                }
            }

            ImageWriter.write(tiffImage, file)
        }
    }

    private fun addPixels(output: DoubleImage, image: DoubleImage) {
        val outputColor = DoubleArray(3)
        val imageColor = DoubleArray(3)
        for (y in 0 until output.height) {
            for (x in 0 until output.width) {
                output.getPixel(x, y, ColorModel.RGB, outputColor)
                image.getPixel(x, y, ColorModel.RGB, imageColor)
                outputColor[0] += imageColor[0]
                outputColor[1] += imageColor[1]
                outputColor[2] += imageColor[2]
                output.setPixel(x, y, ColorModel.RGB, outputColor)
            }
        }
    }

    fun <T> measureElapsed(name: String, func: () -> T): T {
        val startMillis = System.currentTimeMillis()
        val result = func.invoke()
        val endMillis = System.currentTimeMillis()
        val deltaMillis = endMillis - startMillis

        println("$name in $deltaMillis ms")
        return result
    }
}