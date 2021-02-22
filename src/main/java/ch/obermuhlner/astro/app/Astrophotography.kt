package ch.obermuhlner.astro.app

import ch.obermuhlner.astro.app.CommandParser.NamedArguments
import ch.obermuhlner.astro.app.CommandParser.OptionsWithArguments
import ch.obermuhlner.astro.gradient.Point
import ch.obermuhlner.astro.gradient.filter.*
import ch.obermuhlner.astro.gradient.operation.ImageOperation
import ch.obermuhlner.astro.gradient.operation.SubtractLinearImageOperation
import ch.obermuhlner.astro.image.*
import ch.obermuhlner.astro.image.color.ColorModel
import java.io.*
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern
import javax.script.ScriptEngineManager
import javax.script.ScriptException

object Astrophotography {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        runTest()
    }

    @Throws(IOException::class)
    fun mainTest(args: Array<String?>?) {
        val outputFilePrefix = "output_"
        val inputFiles: MutableList<File> = ArrayList()
        val outputFiles: MutableList<File> = ArrayList()
        val gradientFilter = AtomicReference<Filter>(GaussianBlurFilter(100, ColorModel.RGB))
        val subtractOperation = AtomicReference<ImageOperation>(SubtractLinearImageOperation())
        val gradientPoints: MutableList<Point> = ArrayList()
        val sampleRadius = AtomicInteger(3)
        val commandParser = CommandParser()
        /*
        commandParser.add(
                CommandParser.Command("script", 1)
        ) { commandArguments: NamedArguments, optionsWithArguments: OptionsWithArguments? ->
            val script = loadScript(File(commandArguments.arguments[0]))
            commandParser.parse(script)
        }
        commandParser.add(
                CommandParser.Command("input", 1)
        ) { commandArguments: NamedArguments, optionsWithArguments: OptionsWithArguments? -> inputFiles.add(File(commandArguments.arguments[0])) }
        commandParser.add(
                CommandParser.Command("batch", 0,
                        CommandParser.Option("file", 1))
        ) { commandArguments: NamedArguments?, optionsWithArguments: OptionsWithArguments -> optionsWithArguments.handleOption("file") { arguments: List<String?> -> inputFiles.add(File(arguments[0])) } }
        commandParser.add(
                CommandParser.Command("gradient", 0,
                        CommandParser.Option("point", 1),
                        CommandParser.Option("sampleradius", 1),
                        CommandParser.Option("interpolationpower", 1)
                )
        ) { commandArguments: NamedArguments?, optionsWithArguments: OptionsWithArguments ->
            val gradientInterpolationFilter = GradientInterpolationFilter()
            optionsWithArguments.handleOption("point") { arguments: List<String?> -> gradientPoints.add(toPoint(arguments[0])) }
            optionsWithArguments.handleOption("interpolationpower") { arguments: List<String?> -> gradientInterpolationFilter.interpolationPower = arguments[0]!!.toDouble() }
            optionsWithArguments.handleOption("sampleradius") { arguments: List<String?> -> sampleRadius.set(arguments[0]!!.toInt()) }
            gradientFilter.set(gradientInterpolationFilter)
        }
        commandParser.add(
                CommandParser.Command("output", 1)
        ) { commandArguments: NamedArguments, optionsWithArguments: OptionsWithArguments? -> outputFiles.add(File(commandArguments.arguments[0])) }
        */

        commandParser.parse(arrayOf(
                "input", "images/Autosave001.tif",  //        "input", "images/inputs/Autosave001_small_compress0.png",
                //        "median-blur", "10",
                //        "gaussian-blur", "50",
                //        "gradient", "--point", "100,100", "--point", "-100,-100",
                "output", "images/Test.png"
        ))
        //commandParser.parse(args);

        for (f in inputFiles.indices) {
            val inputFile = inputFiles[f]
            var outputFile: File
            outputFile = if (f < outputFiles.size) {
                outputFiles[f]
            } else {
                File(inputFile.parent, outputFilePrefix + inputFile.name)
            }
            println("Load $inputFile")
            val inputImage = ImageReader.read(inputFile, ImageQuality.High)
            if (gradientFilter.get() is GradientInterpolationFilter) {
                val gradientInterpolationFilter = gradientFilter.get() as GradientInterpolationFilter
                gradientInterpolationFilter.setFixPoints(correctFixPoints(gradientPoints, inputImage), inputImage, sampleRadius.get())
            }
            println("Create gradient " + gradientFilter.get())
            val gradientImage = gradientFilter.get().filter(inputImage)
            println("Subtract gradient " + subtractOperation.get())
            val outputImage = ImageCreator.create(inputImage.width, inputImage.height, ImageQuality.High)
            subtractOperation.get().operation(inputImage, gradientImage, outputImage)
            println("Save $outputFile")
            ImageWriter.write(outputImage, outputFile)
            println("Finished $inputFile -> $outputFile")
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
                Arrays.asList(Point(x1, y1), Point(x2, y2), Point(x3, y3)),
                Arrays.asList(color1, color2, color3))
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

    @Throws(IOException::class)
    private fun runTest() {
        val input = loadImage("images/Autosave001.tif")
        var gradient = pseudoMedianFilter(input, 5)
        gradient = gaussianBlur(input, 100)
        saveImage(gradient, "images/TestGradient.png")
        val output = subtractLinear(input, gradient)
        saveImage(output, "images/TestOutput.png")
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

    fun medianBlur(image: DoubleImage, radius: Int): DoubleImage {
        return MedianFilter(radius, ColorModel.RGB).filter(image)
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