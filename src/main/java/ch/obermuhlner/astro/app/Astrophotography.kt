package ch.obermuhlner.astro.app

import ch.obermuhlner.astro.gradient.Point
import ch.obermuhlner.astro.gradient.filter.*
import ch.obermuhlner.astro.gradient.operation.ImageOperation
import ch.obermuhlner.astro.gradient.operation.SubtractImageOperation
import ch.obermuhlner.astro.gradient.operation.SubtractLinearImageOperation
import ch.obermuhlner.astro.gradient.operation.SubtractSplineImageOperation
import ch.obermuhlner.astro.image.*
import ch.obermuhlner.astro.image.color.ColorModel
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern
import javax.script.ScriptEngineManager
import javax.script.ScriptException

enum class GlowStrategy {
    Gradient,
    Blur,
    SingleColor
}

enum class SingleColorStrategy {
    Median,
    Average,
    Darkest
}

enum class SubtractStrategy {
    Subtract,
    SubtractLinear,
    SubtractSpline
}

object Astrophotography {
    @Throws(IOException::class)
    @JvmStatic
    fun mainTEST(args: Array<String>) {
        runTest()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgParser(arrayOf(
                "-h",
                "-v",
                "--blur",
                "images/Autosave001.tif"
        ))

        val verbose by parser.flagging(
                "-v", "--verbose",
                help = "enable verbose mode")

        val glowStrategy by parser.mapping(
                "--gradient" to GlowStrategy.Gradient,
                "--blur" to GlowStrategy.Blur,
                "--single-color" to GlowStrategy.SingleColor,
                help = "glow removal strategy").default(GlowStrategy.Gradient)

        val points by parser.adding(
                "-p", "--point",
                help = "fix point to determine glow gradient") { toPoint(this) }

        val sampleRadius by parser.storing(
                "--sample-radius",
                help = "sample radius") { toInt() }.default(3)

        val interpolationPower by parser.storing(
                "--interpolation-power",
                help = "interpolation power") { toDouble() }.default(3.0)

        val color by parser.storing(
                "--color",
                help = "color") { toColor(this) }.default(DoubleArray(3))

        val singleColorStrategy by parser.mapping(
                "--median-color" to SingleColorStrategy.Median,
                "--average-color" to SingleColorStrategy.Average,
                "--darkest-color" to SingleColorStrategy.Darkest,
                help = "").default(SingleColorStrategy.Median)

        val subtractStrategy by parser.mapping(
                "--subtract" to SubtractStrategy.Subtract,
                "--subtract-linear" to SubtractStrategy.SubtractLinear,
                "--subtract-spline" to SubtractStrategy.SubtractSpline,
                help = "").default(SubtractStrategy.SubtractLinear)

        val splineFactor by parser.storing(
                "--spline-factor",
                help = "spline factor") { toDouble() }.default(0.01)

        val sources by parser.positionalList(
                help = "source filename")

        if (verbose) {
            println("glowStrategy: $glowStrategy")
            println("points: $points")
            println("sampleRadius: $sampleRadius")
            println("interpolationPower: $interpolationPower")
            println("color: $color")
            println("singleColorStrategy: $singleColorStrategy")
            println("sources: $sources")
        }

        for (source in sources) {
            println("Loading $source")
            val sourceFile = File(source)
            val inputImage = ImageReader.read(sourceFile, ImageQuality.High)

            println("Processing $source")
            val glowFilter = when (glowStrategy) {
                GlowStrategy.SingleColor -> {
                    GaussianBlurFilter(5, ColorModel.RGB)
                }
                GlowStrategy.Blur -> {
                    GaussianBlurFilter(100, ColorModel.RGB)
                }
                GlowStrategy.Gradient -> {
                    val gradientFilter = GradientInterpolationFilter(interpolationPower)
                    if (points.isEmpty()) {
                        autoSetFixPoints(gradientFilter, inputImage)
                    } else {
                        gradientFilter.setFixPoints(correctFixPoints(points, inputImage), inputImage, sampleRadius)
                    }
                    gradientFilter
                }
            }

            val gradientImage = glowFilter.filter(inputImage)

            println("Subtracting calculated glow from input image")
            val subtractOperation = when (subtractStrategy) {
                SubtractStrategy.Subtract -> {
                    SubtractImageOperation()
                }
                SubtractStrategy.SubtractLinear -> {
                    SubtractLinearImageOperation()
                }
                SubtractStrategy.SubtractSpline -> {
                    SubtractSplineImageOperation(splineFactor)
                }
            }
            val outputImage = ImageCreator.create(inputImage.width, inputImage.height, ImageQuality.High)
            subtractOperation.operation(inputImage, gradientImage, outputImage)

            val outputFile = File(sourceFile.parent, "output_" + sourceFile.name)
            println("Saving $outputFile")
            ImageWriter.write(outputImage, outputFile)
        }
    }

    private fun autoSetFixPoints(gradientInterpolationFilter: GradientInterpolationFilter, image: DoubleImage) {
        val sampleWidth = image.width / 5
        val sampleHeight = image.height / 5
        val x1 = image.width / 2
        val y1 = sampleHeight / 2
        val color1 = image.medianPixel(x1, y1, sampleWidth, sampleHeight)
        println("Auto median pixel1: " + Arrays.toString(color1))
        val x2 = sampleWidth / 2
        val y2 = image.height - sampleHeight / 2
        val color2 = image.medianPixel(x2, y2, sampleWidth, sampleHeight)
        println("Auto median pixel2: " + Arrays.toString(color2))
        val x3 = image.width - sampleWidth / 2
        val y3 = image.height - sampleHeight / 2
        val color3 = image.medianPixel(x3, y3, sampleWidth, sampleHeight)
        println("Auto median pixel3: " + Arrays.toString(color3))
        gradientInterpolationFilter.setFixPoints(
                listOf(Point(x1, y1), Point(x2, y2), Point(x3, y3)),
                listOf(color1, color2, color3))
    }

    private fun loadScript(file: File): Array<String> {
        return try {
            val script = Files.readString(file.toPath())
            script.split("\\s+".toRegex()).toTypedArray()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun correctFixPoints(fixPoints: List<Point>, inputImage: DoubleImage): List<Point> {
        val result: MutableList<Point> = ArrayList()
        for (fixPoint in fixPoints) {
            var fp = fixPoint
            if (fp.x < 0) {
                fp = Point(inputImage.width + fp.x, fp.y)
            }
            if (fp.y < 0) {
                fp = Point(fixPoint.x, inputImage.height + fp.y)
            }
            result.add(fp)
        }
        return result
    }

    private fun toPoint(string: String): Point {
        val split = string.split(Pattern.quote(",").toRegex()).toTypedArray()
        return Point(split[0].toInt(), split[1].toInt())
    }

    private fun toColor(string: String): DoubleArray {
        val split = string.split(',')
        return DoubleArray(3) { split[it].toDouble() }
    }

    @Throws(IOException::class)
    private fun runTest() {
        val input = loadImage("images/Autosave001.tif")
        val nostars1 = pseudoMedianFilter(input, 20)
        saveImage(nostars1, "images/TestNoStars1.png")
        val nostars2 = gaussianBlur(nostars1, 2)
        saveImage(nostars2, "images/TestNoStars2.png")
        val stars1 = subtractLinear(input, nostars1)
        saveImage(stars1, "images/TestStars1.png")
        val stars2 = subtractLinear(input, nostars2)
        saveImage(stars2, "images/TestStars2.png")
    }

    @Throws(ScriptException::class)
    private fun runScript() {
        val manager = ScriptEngineManager()
        val engine = manager.getEngineByName("jshell")
        val script = "" +
                "import static ch.obermuhlner.astro.app.Astrophotography.*;" +
                "var input = loadImage(\"images/Autosave001.tif\");" +
                "var gradient = gaussianBlur(input, 200);" +
                "var output = subtractLinear(input, gradient);" +
                "saveImage(output, \"images/Test.png\");"
        engine.eval(script)
    }

    @Throws(IOException::class)
    fun loadImage(filename: String): DoubleImage {
        return ImageReader.read(File(filename), ImageQuality.High)
    }

    @Throws(IOException::class)
    fun saveImage(image: DoubleImage, filename: String?) {
        ImageWriter.write(image, File(filename))
    }

    fun gaussianBlur(image: DoubleImage, radius: Int): DoubleImage {
        return GaussianBlurFilter(radius, ColorModel.RGB).filter(image)
    }

    fun medianFilter(image: DoubleImage, radius: Int): DoubleImage {
        return MedianFilter(radius).filter(image)
    }

    fun horizontalMedianFilter(image: DoubleImage, radius: Int): DoubleImage {
        return HorizontalMedianFilter(radius, ColorModel.RGB).filter(image)
    }

    fun verticalMedianFilter(image: DoubleImage, radius: Int): DoubleImage {
        return VerticalMedianFilter(radius, ColorModel.RGB).filter(image)
    }

    fun pseudoMedianFilter(image: DoubleImage, radius: Int): DoubleImage {
        return PseudoMedianFilter(radius, ColorModel.RGB).filter(image)
    }

    fun gradient(image: DoubleImage): DoubleImage {
        val gradientInterpolationFilter = GradientInterpolationFilter()
        autoSetFixPoints(gradientInterpolationFilter, image)
        return gradientInterpolationFilter.filter(image)
    }

    fun subtractLinear(image1: DoubleImage, image2: DoubleImage): DoubleImage {
        return SubtractLinearImageOperation().operation(image1, image2)
    }
}