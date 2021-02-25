package ch.obermuhlner.astro.javafx

import ch.obermuhlner.astro.gradient.Point
import ch.obermuhlner.astro.gradient.analysis.Histogram
import ch.obermuhlner.astro.gradient.filter.GaussianBlurFilter
import ch.obermuhlner.astro.gradient.filter.GradientInterpolationFilter
import ch.obermuhlner.astro.gradient.filter.PseudoMedianFilter
import ch.obermuhlner.astro.gradient.operation.ImageOperation
import ch.obermuhlner.astro.gradient.operation.SubtractLinearImageOperation
import ch.obermuhlner.astro.image.*
import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorUtil
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.common.GenericImageMetadata
import java.io.*
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function
import java.util.function.Supplier
import kotlin.math.max
import kotlin.math.min

class AstrophotographyApp : Application() {
    private val homeDirectory = homeDirectory()
    private val gradientInterpolationFilter = GradientInterpolationFilter()
    private var gradientSubtractor: ImageOperation = SubtractLinearImageOperation()
    private val zoomCenterXProperty: IntegerProperty = SimpleIntegerProperty()
    private val zoomCenterYProperty: IntegerProperty = SimpleIntegerProperty()
    private val zoomDeltaSampleChannelProperty: ObjectProperty<SampleChannel> = SimpleObjectProperty(SampleChannel.Brightness)
    private val zoomDeltaSampleFactorProperty: DoubleProperty = SimpleDoubleProperty()
    private val glowStrategyProperty: ObjectProperty<GlowStrategy> = SimpleObjectProperty()
    private val sampleRadiusProperty: IntegerProperty = SimpleIntegerProperty()
    private val samplePixelColorProperty: ObjectProperty<Color> = SimpleObjectProperty()
    private val sampleAverageColorProperty: ObjectProperty<Color> = SimpleObjectProperty()
    private val gradientPixelColorProperty: ObjectProperty<Color> = SimpleObjectProperty()
    private val pointFinderStrategyProperty: ObjectProperty<PointFinderStrategy> = SimpleObjectProperty()
    private val interpolationPowerProperty: DoubleProperty = SimpleDoubleProperty()
    private val despeckleRadiusProperty: IntegerProperty = SimpleIntegerProperty()
    private val blurRadiusProperty: IntegerProperty = SimpleIntegerProperty()
    private val singleGlowColorProperty: ObjectProperty<Color?> = SimpleObjectProperty(Color.BLACK)
    private val singleGlowColorDescriptionProperty: StringProperty = SimpleStringProperty()
    private val singleGlowColorUpdate = AtomicReference<Color?>()

    @Volatile
    private var medianAllColor: Color? = null

    @Volatile
    private var averageAllColor: Color? = null

    @Volatile
    private var darkestAllColor: Color? = null
    private val removalFactorProperty: DoubleProperty = SimpleDoubleProperty()
    private val sampleSubtractionStrategyProperty: ObjectProperty<SubtractionStrategy> = SimpleObjectProperty(SubtractionStrategy.SubtractLinear)
    private val crosshairColors = listOf(Color.YELLOW, Color.RED, Color.GREEN, Color.BLUE, Color.TRANSPARENT)
    private val crosshairColorProperty: ObjectProperty<Color> = SimpleObjectProperty(crosshairColors[0])
    private val fixPointColorProperty: ObjectProperty<Color> = SimpleObjectProperty(crosshairColors[1])

    private var inputFile: File? = null
    private var inputDecorationsPane: Pane? = null
    private var gradientDecorationsPane: Pane? = null
    private var outputDecorationsPane: Pane? = null
    private var deltaDecorationsPane: Pane? = null
    private var inputImage: WritableImage = DummyWritableImage
    private var inputDoubleImage: DoubleImage = DummyDoubleImage
    private var inputImageView: ImageView? = null
    private var gradientImage: WritableImage = DummyWritableImage
    private var gradientDoubleImage: DoubleImage = DummyDoubleImage
    private var gradientImageView: ImageView? = null
    private var outputImage: WritableImage = DummyWritableImage
    private var outputDoubleImage: DoubleImage = DummyDoubleImage
    private var outputImageView: ImageView? = null
    private var deltaImage: WritableImage = DummyWritableImage
    private var deltaDoubleImage: DoubleImage = DummyDoubleImage
    private var deltaImageView: ImageView? = null
    private var zoomInputImage: WritableImage = DummyWritableImage
    private var zoomInputDoubleImage: DoubleImage = DummyDoubleImage
    private var zoomInputImageView: ImageView? = null
    private var zoomOutputImage: WritableImage = DummyWritableImage
    private var zoomOutputDoubleImage: DoubleImage = DummyDoubleImage
    private var zoomOutputImageView: ImageView? = null
    private var zoomGradientImage: WritableImage = DummyWritableImage
    private var zoomGradientDoubleImage: DoubleImage = DummyDoubleImage
    private var zoomGradientImageView: ImageView? = null
    private var zoomDeltaImage: WritableImage = DummyWritableImage
    private var zoomDeltaDoubleImage: DoubleImage = DummyDoubleImage
    private var zoomDeltaImageView: ImageView? = null

    private val fixPoints = FXCollections.observableArrayList<FixPoint>()

    private var sampleRadiusSpinner: Spinner<Number>? = null
    private var colorCurveCanvas: Canvas? = null
    private val inputHistogram = Histogram(ColorModel.RGB, HISTOGRAM_WIDTH - 1)
    private var inputHistogramCanvas: Canvas? = null
    private val zoomInputHistogram = Histogram(ColorModel.RGB, HISTOGRAM_WIDTH - 1)
    private var zoomInputHistogramCanvas: Canvas? = null
    private val zoomOutputHistogram = Histogram(ColorModel.RGB, HISTOGRAM_WIDTH - 1)
    private var zoomOutputHistogramCanvas: Canvas? = null
    private val imageInfoGridPane = GridPane()

    override fun start(primaryStage: Stage) {
        val root = Group()
        val scene = Scene(root)
        val vbox = VBox(SPACING)
        root.children.add(vbox)
        vbox.children.add(createToolbar(primaryStage))
        val hbox = HBox(SPACING)
        vbox.children.add(hbox)
        run {
            val imageTabPane = TabPane()
            hbox.children.add(imageTabPane)
            imageTabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            val inputTab = Tab("Input", createInputImageViewer())
            imageTabPane.tabs.add(inputTab)
            inputTab.tooltip = tooltip(("Shows the input image with several overlays:\n"
                    + "- the zoom window\n"
                    + "- the fixpoints for gradient interpolation (initially empty)"))

            val glowTab = Tab("Glow", createGradientImageViewer())
            imageTabPane.tabs.add(glowTab)
            glowTab.tooltip = tooltip(("Shows the calculated sky glow image that will be removed from the input image.\n"
                    + "Switching away from the input tab will take a while to calculate the image."))
            val outputTab = Tab("Output", createOutputImageViewer())
            imageTabPane.tabs.add(outputTab)
            outputTab.tooltip = tooltip(("Shows the calculated output image with the sky glow removed from the input image.\n"
                    + "Switching away from the input tab will take a while to calculate the image."))
            val deltaTab = Tab("Delta", createDeltaImageViewer())
            imageTabPane.tabs.add(deltaTab)
            outputTab.tooltip = tooltip(("Shows the difference between the glow image and input image.\n"
                    + "The channel to calculate the difference can be selected: Red, Green, Blue, Hue, Saturation, Brightness\n"
                    + "Blue colors indicate a positive, red a negative difference.\n"
                    + "Switching away from the input tab will take a while to calculate the image."))
            val infoTab = Tab("Info", createInfoPane())
            imageTabPane.tabs.add(infoTab)
            imageTabPane.selectionModel.selectedItemProperty().addListener { _, oldValue: Tab, newValue: Tab ->
                if (oldValue === inputTab && newValue !== infoTab) {
                    removeGradient(inputDoubleImage, gradientDoubleImage, outputDoubleImage)
                    calculateDeltaImage(
                            inputDoubleImage,
                            gradientDoubleImage,
                            deltaDoubleImage,
                            zoomDeltaSampleChannelProperty.get().colorModel,
                            zoomDeltaSampleChannelProperty.get().sampleIndex,
                            zoomDeltaSampleFactorProperty.get())
                }
            }
        }
        run { hbox.children.add(createEditor()) }
        primaryStage.scene = scene
        primaryStage.show()
        glowStrategyProperty.addListener { _, _, _ -> updateZoom() }
        fixPoints.addListener(ListChangeListener { updateFixPoints() })
        sampleRadiusProperty.addListener { _, _, _ -> updateFixPoints() }
        pointFinderStrategyProperty.addListener { _, _, _ ->
            gradientInterpolationFilter.setPointsFinder(pointFinderStrategyProperty.get().pointsFinder)
            updateZoom()
        }
        interpolationPowerProperty.addListener { _, _, _ ->
            gradientInterpolationFilter.interpolationPower = interpolationPowerProperty.get()
            updateZoom()
        }
        singleGlowColorProperty.addListener { _, _, _ -> updateZoom() }
        despeckleRadiusProperty.addListener { _, _, _ -> updateZoom() }
        blurRadiusProperty.addListener { _, _, _ -> updateZoom() }
//    removalFactor.addListener((observable, oldValue, newValue) -> {
//      gradientInterpolationFilter.setRemovalFactor(removalFactor.get());
//      updateZoom();
//    });
        sampleSubtractionStrategyProperty.addListener { _, _, _ ->
            gradientSubtractor = sampleSubtractionStrategyProperty.get().operation
            updateZoom()
        }
        zoomCenterXProperty.addListener { _, _, _ -> updateZoom() }
        zoomCenterYProperty.addListener { _, _, _ -> updateZoom() }
        zoomDeltaSampleChannelProperty.addListener { _, _, _ -> updateZoomDelta() }
        zoomDeltaSampleFactorProperty.addListener { _, _, _ -> updateZoomDelta() }

        initializeValues()

        loadImage(DummyDoubleImage)
    }

    private fun createInfoPane(): Node {
        val scrollPane = ScrollPane(imageInfoGridPane)

        imageInfoGridPane.hgap = SPACING
        imageInfoGridPane.vgap = SPACING

        return scrollPane
    }

    private fun updateInfoPane(file: File) {
        imageInfoGridPane.getChildren().clear()
        var rowIndex = 0

        val metadata = Imaging.getMetadata(file)
        for (item in metadata.items) {
            when (item) {
                is GenericImageMetadata.GenericImageMetadataItem -> {
                    imageInfoGridPane.add(Label(item.keyword), 0, rowIndex)
                    imageInfoGridPane.add(Label(item.text), 1, rowIndex)
                }
                else -> {
                    imageInfoGridPane.add(Label(item.toString()), 0, rowIndex)
                }
            }
            rowIndex++
        }
    }

    private fun removeGradient(input: DoubleImage, gradient: DoubleImage, output: DoubleImage) {
        when (glowStrategyProperty.get()) {
            GlowStrategy.SingleColor -> {
                gradient.setPixels(ColorModel.RGB, toDoubleColorRGB(singleGlowColorProperty.get()!!))
            }
            GlowStrategy.Blur -> {
                val despeckleFilter = PseudoMedianFilter(despeckleRadiusProperty.get(), ColorModel.RGB)
                val despeckled = despeckleFilter.filter(input)
                val gaussianBlurFilter = GaussianBlurFilter(blurRadiusProperty.get(), ColorModel.RGB)
                gaussianBlurFilter.filter(despeckled, gradient)
            }
            GlowStrategy.Gradient -> {
                gradientInterpolationFilter.filter(input, gradient)
            }
        }
        gradientSubtractor.operation(input, gradient, output)
    }

    private fun initializeValues() {
        pointFinderStrategyProperty.set(PointFinderStrategy.All)
        setSampleRadius(5)
        interpolationPowerProperty.set(3.0)
        despeckleRadiusProperty.set(5)
        blurRadiusProperty.set(100)
        removalFactorProperty.set(1.0)
        sampleSubtractionStrategyProperty.set(SubtractionStrategy.SubtractLinear)
    }

    private fun setSampleRadius(value: Int) {
        // workaround, because Spinner.valueProperty() is read only
        sampleRadiusSpinner!!.valueFactory.value = value
    }

    private fun updateFixPoints() {
        inputDecorationsPane!!.children.clear()
        for (fixPoint in fixPoints) {
            val circle = Circle(3.0)
            circle.fill = Color.TRANSPARENT
            circle.strokeProperty().bind(fixPointColorProperty)
            val x = fixPoint.x / inputImage.width * inputImageView!!.boundsInLocal.width
            val y = fixPoint.y / inputImage.height * inputImageView!!.boundsInLocal.height
            circle.centerX = x
            circle.centerY = y
            inputDecorationsPane!!.children.add(circle)
        }
        gradientInterpolationFilter.setFixPoints(
                toPointList(fixPoints),
                inputDoubleImage,
                sampleRadiusProperty.get())
        updateZoom()
    }

    private fun createToolbar(stage: Stage): Node {
        val box = HBox(SPACING)
        val openImageButton = Button("Open Image ...")
        openImageButton.tooltip = tooltip("Opens a new input image.")
        val openProjectButton = Button("Open Project ...")
        openProjectButton.tooltip = tooltip("Opens an EZ-Astro project.")
        val saveButton = Button("Save ...")
        saveButton.tooltip = tooltip("Saves the calculated output image.")
        saveButton.isDisable = true

        box.children.add(openImageButton)
        openImageButton.onAction = EventHandler {
            openImageFile(stage)
            saveButton.isDisable = false
        }

        box.children.add(openProjectButton)
        openProjectButton.onAction = EventHandler {
            openProjectFile(stage)
            saveButton.isDisable = false
        }

        box.children.add(saveButton)
        saveButton.onAction = EventHandler { saveImageFile(stage) }

        val crosshairColorButton = createColorButton(crosshairColorProperty, Rectangle(10.0, 10.0))
        crosshairColorButton.tooltip = tooltip("Toggles the color of the crosshair in the zoom images.")
        box.children.add(crosshairColorButton)
        val fixPointColorButton = createColorButton(fixPointColorProperty, Circle(3.0))
        fixPointColorButton.tooltip = tooltip("Toggles the color of the fix point markers in the input images.")
        box.children.add(fixPointColorButton)
        return box
    }

    private fun createColorButton(colorProperty: ObjectProperty<Color>, shape: Shape): Button {
        val button = Button()
        shape.fill = Color.TRANSPARENT
        shape.strokeProperty().bind(colorProperty)
        button.graphic = shape
        button.onAction = EventHandler {
            var index = crosshairColors.indexOf(colorProperty.get())
            index = (index + 1) % crosshairColors.size
            colorProperty.setValue(crosshairColors[index])
        }
        return button
    }

    private fun openImageFile(stage: Stage) {
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = homeDirectory.toFile()
        fileChooser.title = "Open Input Image"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image", "*.tif", "*.tiff", "*.png", "*.jpg", "*.jpeg"))
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("All", "*"))
        val chosenFile = fileChooser.showOpenDialog(stage)
        if (chosenFile != null) {
            ProgressDialog.show("Loading", "Loading input image ...") {
                try {
                    inputFile = chosenFile
                    loadImage(chosenFile)
                    updateInfoPane(chosenFile)
                    stage.title = chosenFile.name
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun openProjectFile(stage: Stage) {
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = homeDirectory.toFile()
        fileChooser.title = "Open Input Image"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("EZ-Astro", "*" + EZ_ASTRO_FILE_EXTENSION))
        val propertiesFile = fileChooser.showOpenDialog(stage)
        if (propertiesFile != null) {
            ProgressDialog.show("Loading", "Loading EZ-Astro project ...") {
                try {
                    val imageFile = loadProperties(propertiesFile)
                    inputFile = imageFile
                    stage.title = imageFile.name + " - " + propertiesFile.name
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveImageFile(stage: Stage) {
        var directory: File? = null
        if (inputFile != null) {
            directory = inputFile!!.parentFile
        }
        if (directory == null) {
            homeDirectory.toFile()
        }
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = directory
        fileChooser.title = "Save Image"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image", "*.tif", "*.tiff", "*.png", "*.jpg", "*.jpeg"))
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("All", "*"))
        val outputFile = fileChooser.showSaveDialog(stage)
        if (outputFile != null) {
            ProgressDialog.show("Saving", "Saving output image ...") {
                try {
                    val propertiesFile = saveImage(outputFile)
                    stage.title = inputFile!!.name + " - " + propertiesFile.name
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun saveImage(outputFile: File): File {
        val inputImage = inputDoubleImage
        //DoubleImage inputImage = ImageReader.read(inputFile, ImageQuality.High);
        val outputImage = createOutputImage(inputImage)
        removeGradient(inputImage, gradientDoubleImage, outputImage)
        ImageWriter.write(outputImage, outputFile)

        val propertiesFile = toPropertiesFile(outputFile)
        saveProperties(propertiesFile)
        return propertiesFile
    }

    private fun toPropertiesFile(outputFile: File): File {
        return File(outputFile.path + EZ_ASTRO_FILE_EXTENSION)
    }

    @Throws(IOException::class)
    private fun saveProperties(file: File) {
        PrintWriter(FileWriter(file)).use { writer ->
            val properties = Properties()
            properties["version"] = "0.0.2"
            properties["input"] = inputFile.toString()
            properties["glow.remover"] = glowStrategyProperty.get().toString()
            properties["glow.remover.singleColor.color"] = singleGlowColorProperty.get().toString()
            properties["glow.remover.singleColor.colorDescription"] = singleGlowColorDescriptionProperty.get().toString()
            properties["glow.remover.blur.despeckleRadius"] = despeckleRadiusProperty.get().toString()
            properties["glow.remover.blur.gaussianBlurRadius"] = blurRadiusProperty.get().toString()
            for (i in fixPoints.indices) {
                properties["glow.remover.gradient.fixpoint.$i.x"] = fixPoints[i].x.toString()
                properties["glow.remover.gradient.fixpoint.$i.y"] = fixPoints[i].y.toString()
            }
            properties["glow.remover.gradient.sampleRadius"] = sampleRadiusProperty.get().toString()
            properties["glow.remover.gradient.removalFactor"] = removalFactorProperty.get().toString()
            properties["glow.remover.gradient.interpolationPower"] = interpolationPowerProperty.get().toString()
            properties["glow.remover.sampleSubtractionStrategy"] = sampleSubtractionStrategyProperty.get().name
            properties.store(writer, "EZ-Astrophotography\nhttps://github.com/eobermuhlner/ez-astrophotography")
        }
    }

    @Throws(IOException::class)
    private fun loadProperties(file: File): File {
        var result: File
        FileReader(file).use { reader ->
            val properties = Properties()
            properties.load(reader)
            val version = properties.getProperty("version")
            if (!version.startsWith("0.")) {
                throw IOException("Incompatible EZ-Astrophotography version: $version")
            }
            result = File(properties.getProperty("input"))
            loadImage(result)
            sampleSubtractionStrategyProperty.set(SubtractionStrategy.valueOf(properties.getProperty("glow.remover.sampleSubtractionStrategy")))
            glowStrategyProperty.set(GlowStrategy.valueOf(properties.getProperty("glow.remover")))
            singleGlowColorProperty.set(Color.valueOf(properties.getProperty("glow.remover.singleColor.color")))
            singleGlowColorDescriptionProperty.set(properties.getProperty("glow.remover.singleColor.colorDescription"))
            despeckleRadiusProperty.set(properties.getProperty("glow.remover.blur.despeckleRadius").toInt())
            blurRadiusProperty.set(properties.getProperty("glow.remover.blur.gaussianBlurRadius").toInt())
            setSampleRadius(properties.getProperty("glow.remover.gradient.sampleRadius").toInt())
            removalFactorProperty.set(properties.getProperty("glow.remover.gradient.removalFactor").toDouble())
            interpolationPowerProperty.set(properties.getProperty("glow.remover.gradient.interpolationPower").toDouble())
            fixPoints.clear()
            var fixPointLoading = true
            var fixPointIndex = 0
            while (fixPointLoading) {
                val x = properties.getProperty("glow.remover.gradient.fixpoint.$fixPointIndex.x")
                val y = properties.getProperty("glow.remover.gradient.fixpoint.$fixPointIndex.y")
                if (x != null && y != null) {
                    addFixPoint(x.toInt(), y.toInt())
                } else {
                    fixPointLoading = false
                }
                fixPointIndex++
            }
        }
        return result
    }

    private fun createOutputImage(inputImage: DoubleImage): DoubleImage {
        val width = inputImage.width
        val height = inputImage.height
        return ImageCreator.create(width, height, ImageQuality.High)
    }

    @Throws(IOException::class)
    private fun loadImage(file: File) {
        loadImage(ImageReader.read(file, ImageQuality.High))
    }

    private fun loadImage(image: DoubleImage) {
        inputDoubleImage = image
        val width = inputDoubleImage.width
        val height = inputDoubleImage.height
        inputImage = WritableImage(width, height)
        val rgb = DoubleArray(3)
        val pw = inputImage.pixelWriter
        for (x in 0 until width) {
            for (y in 0 until height) {
                inputDoubleImage.getPixel(x, y, ColorModel.RGB, rgb)
                pw.setArgb(x, y, ColorUtil.toIntARGB(rgb))
            }
        }
        inputImageView!!.image = inputImage
        gradientImage = WritableImage(width, height)
        gradientDoubleImage = WriteThroughArrayDoubleImage(JavaFXWritableDoubleImage(gradientImage), ColorModel.RGB)
        gradientImageView!!.image = gradientImage
        outputImage = WritableImage(width, height)
        outputDoubleImage = WriteThroughArrayDoubleImage(JavaFXWritableDoubleImage(outputImage), ColorModel.RGB)
        outputImageView!!.image = outputImage
        deltaImage = WritableImage(width, height)
        deltaDoubleImage = WriteThroughArrayDoubleImage(JavaFXWritableDoubleImage(deltaImage), ColorModel.RGB)
        deltaImageView!!.image = deltaImage
        medianAllColor = null
        averageAllColor = null
        darkestAllColor = null
        // TODO consider background thread to fill the colors above
        fixPoints.clear()
        setZoom(width / 2, height / 2)
        updateInputHistogram()
    }

    private fun toPointList(fixPoints: ObservableList<FixPoint>): List<Point> {
        val points: MutableList<Point> = ArrayList()
        for (fixPoint in fixPoints) {
            points.add(Point(fixPoint.x, fixPoint.y))
        }
        return points
    }

    private fun createInputImageViewer(): Node {
        val box = VBox(SPACING)
        inputDecorationsPane = Pane()
        inputDecorationsPane!!.isMouseTransparent = true
        inputImageView = ImageView()
        box.children.add(withZoomRectangle(inputImageView!!, inputDecorationsPane!!))
        inputImageView!!.isPreserveRatio = true
        inputImageView!!.fitWidth = IMAGE_WIDTH.toDouble()
        inputImageView!!.fitHeight = IMAGE_HEIGHT.toDouble()
        setupImageSelectionListener(inputImageView!!)
        return box
    }

    private fun createGradientImageViewer(): Node {
        val box = VBox(SPACING)
        gradientDecorationsPane = Pane()
        gradientDecorationsPane!!.isMouseTransparent = true
        gradientImageView = ImageView()
        box.children.add(withZoomRectangle(gradientImageView!!, gradientDecorationsPane!!))
        gradientImageView!!.isPreserveRatio = true
        gradientImageView!!.fitWidth = IMAGE_WIDTH.toDouble()
        gradientImageView!!.fitHeight = IMAGE_HEIGHT.toDouble()
        setupImageSelectionListener(gradientImageView!!)
        return box
    }

    private fun createOutputImageViewer(): Node {
        val box = VBox(SPACING)
        outputDecorationsPane = Pane()
        outputDecorationsPane!!.isMouseTransparent = true
        outputImageView = ImageView()
        box.children.add(withZoomRectangle(outputImageView!!, outputDecorationsPane!!))
        outputImageView!!.isPreserveRatio = true
        outputImageView!!.fitWidth = IMAGE_WIDTH.toDouble()
        outputImageView!!.fitHeight = IMAGE_HEIGHT.toDouble()
        setupImageSelectionListener(outputImageView!!)
        return box
    }

    private fun createDeltaImageViewer(): Node {
        val box = VBox(SPACING)
        deltaDecorationsPane = Pane()
        deltaDecorationsPane!!.isMouseTransparent = true
        deltaImageView = ImageView()
        box.children.add(withZoomRectangle(deltaImageView!!, deltaDecorationsPane!!))
        deltaImageView!!.isPreserveRatio = true
        deltaImageView!!.fitWidth = IMAGE_WIDTH.toDouble()
        deltaImageView!!.fitHeight = IMAGE_HEIGHT.toDouble()
        setupImageSelectionListener(deltaImageView!!)
        return box
    }

    private fun setupImageSelectionListener(imageView: ImageView) {
        setMouseDragEvents(imageView) { event: MouseEvent ->
            val imageViewWidth = imageView.boundsInLocal.width
            val imageViewHeight = imageView.boundsInLocal.height
            var zoomX = (event.x * imageView.image.width / imageViewWidth).toInt()
            var zoomY = (event.y * imageView.image.height / imageViewHeight).toInt()
            zoomX = max(zoomX, 0)
            zoomY = max(zoomY, 0)
            zoomX = min(zoomX, imageView.image.width.toInt() - 1)
            zoomY = min(zoomY, imageView.image.height.toInt() - 1)
            setZoom(zoomX, zoomY)
        }
    }

    private fun setZoom(x: Int, y: Int) {
        zoomCenterXProperty.set(x)
        zoomCenterYProperty.set(y)
        updateZoom(x, y)
    }

    private fun updateZoom(zoomX: Int = zoomCenterXProperty.get(), zoomY: Int = zoomCenterYProperty.get()) {
        val zoomOffsetX = zoomX - ZOOM_WIDTH / 2
        val zoomOffsetY = zoomY - ZOOM_HEIGHT / 2
        val rgb = DoubleArray(3)
        ColorUtil.toIntARGB(inputDoubleImage.getPixel(zoomX, zoomY, ColorModel.RGB, rgb))
        samplePixelColorProperty.set(Color(rgb[ColorModel.RGB.R], rgb[ColorModel.RGB.G], rgb[ColorModel.RGB.B], 1.0))
        val sampleRadius1 = sampleRadiusProperty.get()
        inputDoubleImage.averagePixel(
                zoomX - sampleRadius1,
                zoomY - sampleRadius1,
                sampleRadius1 + sampleRadius1 + 1,
                sampleRadius1 + sampleRadius1 + 1,
                ColorModel.RGB,
                rgb
        )
        sampleAverageColorProperty.set(Color(rgb[ColorModel.RGB.R], rgb[ColorModel.RGB.G], rgb[ColorModel.RGB.B], 1.0))
        zoomGradientDoubleImage.getPixel(ZOOM_WIDTH / 2, ZOOM_HEIGHT / 2, ColorModel.RGB, rgb)
        gradientPixelColorProperty.set(Color(rgb[ColorModel.RGB.R], rgb[ColorModel.RGB.G], rgb[ColorModel.RGB.B], 1.0))
        zoomInputDoubleImage.setPixels(
                zoomOffsetX,
                zoomOffsetY,
                inputDoubleImage,
                0,
                0,
                ZOOM_WIDTH,
                ZOOM_HEIGHT,
                ColorModel.RGB, doubleArrayOf(0.0, 0.0, 0.0))
        removeGradient(
                zoomInputDoubleImage,
                zoomGradientDoubleImage,
                zoomOutputDoubleImage)
        updateColorCurve()
        updateZoomHistogram()
        updateZoomDelta()
    }

    private fun updateColorCurve() {
        val subtractor = sampleSubtractionStrategyProperty.get().operation
        drawColorCurve(colorCurveCanvas!!, subtractor, gradientPixelColorProperty.get())
    }

    private fun drawColorCurve(canvas: Canvas, subtractor: ImageOperation, gradientColor: Color) {
        val gc = canvas.graphicsContext2D
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height
        val inset = 2.0
        val chartWidth = canvasWidth - inset * 2
        val chartHeight = canvasHeight - inset * 2
        val input: DoubleImage = ArrayDoubleImage(1, 1, ColorModel.RGB)
        val gradient: DoubleImage = ArrayDoubleImage(1, 1, ColorModel.RGB)
        val output: DoubleImage = ArrayDoubleImage(1, 1, ColorModel.RGB)
        val color = DoubleArray(3)
        gc.fill = Color.LIGHTGRAY
        gc.fillRect(0.0, 0.0, canvasWidth, canvasHeight)
        gc.lineWidth = 2.0
        val xStep = 1.0 / canvasWidth * 0.5
        var lastCanvasX = 0.0
        var lastCanvasYR = 0.0
        var lastCanvasYG = 0.0
        var lastCanvasYB = 0.0
        var x = 0.0
        while (x <= 1.0) {
            color[ColorModel.RGB.R] = x
            color[ColorModel.RGB.G] = x
            color[ColorModel.RGB.B] = x
            input.setPixel(0, 0, ColorModel.RGB, color)
            color[ColorModel.RGB.R] = gradientColor.red
            color[ColorModel.RGB.G] = gradientColor.green
            color[ColorModel.RGB.B] = gradientColor.blue
            gradient.setPixel(0, 0, ColorModel.RGB, color)
            subtractor.operation(input, gradient, output)
            output.getPixel(0, 0, ColorModel.RGB, color)
            val yR = color[ColorModel.RGB.R]
            val yG = color[ColorModel.RGB.G]
            val yB = color[ColorModel.RGB.B]
            val canvasX = x * chartWidth + inset
            val canvasYR = canvasHeight - inset - yR * chartHeight
            val canvasYG = canvasHeight - inset - yG * chartHeight
            val canvasYB = canvasHeight - inset - yB * chartHeight
            if (x != 0.0) {
                gc.stroke = RED_SEMI
                gc.strokeLine(lastCanvasX, lastCanvasYR, canvasX, canvasYR)
                gc.stroke = GREEN_SEMI
                gc.strokeLine(lastCanvasX, lastCanvasYG, canvasX, canvasYG)
                gc.stroke = BLUE_SEMI
                gc.strokeLine(lastCanvasX, lastCanvasYB, canvasX, canvasYB)
            }
            lastCanvasX = canvasX
            lastCanvasYR = canvasYR
            lastCanvasYG = canvasYG
            lastCanvasYB = canvasYB
            x += xStep
        }
    }

    private fun updateInputHistogram() {
        inputHistogram.sampleImage(inputDoubleImage)
        drawHistogram(inputHistogramCanvas, inputHistogram)
    }

    private fun updateZoomHistogram() {
        zoomInputHistogram.sampleImage(zoomInputDoubleImage)
        drawHistogram(zoomInputHistogramCanvas, zoomInputHistogram)
        zoomOutputHistogram.sampleImage(zoomOutputDoubleImage)
        drawHistogram(zoomOutputHistogramCanvas, zoomOutputHistogram)
    }

    private fun drawHistogram(histogramCanvas: Canvas?, histogram: Histogram) {
        val gc = histogramCanvas!!.graphicsContext2D
        val canvasWidth = histogramCanvas.width
        val canvasHeight = histogramCanvas.height
        gc.fill = Color.LIGHTGRAY
        gc.fillRect(0.0, 0.0, canvasWidth, canvasHeight)
        gc.lineWidth = 2.0
        var prevR = histogram[ColorModel.RGB.R, 0] * canvasHeight
        var prevG = histogram[ColorModel.RGB.G, 0] * canvasHeight
        var prevB = histogram[ColorModel.RGB.B, 0] * canvasHeight
        for (binIndex in 1 until histogram.binCount) {
            gc.stroke = RED_SEMI
            val r = histogram[ColorModel.RGB.R, binIndex] * canvasHeight
            gc.strokeLine((binIndex - 1).toDouble(), canvasHeight - prevR, binIndex.toDouble(), canvasHeight - r)
            prevR = r
            gc.stroke = GREEN_SEMI
            val g = histogram[ColorModel.RGB.G, binIndex] * canvasHeight
            gc.strokeLine((binIndex - 1).toDouble(), canvasHeight - prevG, binIndex.toDouble(), canvasHeight - g)
            prevG = g
            gc.stroke = BLUE_SEMI
            val b = histogram[ColorModel.RGB.B, binIndex] * canvasHeight
            gc.strokeLine((binIndex - 1).toDouble(), canvasHeight - prevB, binIndex.toDouble(), canvasHeight - b)
            prevB = b
        }
    }

    private fun updateZoomDelta() {
        calculateDeltaImage(
                zoomInputDoubleImage,
                zoomGradientDoubleImage,
                zoomDeltaDoubleImage,
                zoomDeltaSampleChannelProperty.get().colorModel,
                zoomDeltaSampleChannelProperty.get().sampleIndex,
                zoomDeltaSampleFactorProperty.get())
    }

    private fun calculateDeltaImage(image1: DoubleImage, image2: DoubleImage, deltaImage: DoubleImage, colorModel: ColorModel, sampleIndex: Int, sampleFactor: Double) {
        val sample1 = DoubleArray(3)
        val sample2 = DoubleArray(3)
        val output = DoubleArray(3)
        val rgb = DoubleArray(3)
        for (y in 0 until image1.height) {
            for (x in 0 until image1.width) {
                image1.getPixel(x, y, ColorModel.RGB, sample1)
                image2.getPixel(x, y, ColorModel.RGB, sample2)
                output[ColorModel.RGB.R] = sample1[ColorModel.RGB.R] - sample2[ColorModel.RGB.R]
                output[ColorModel.RGB.G] = sample1[ColorModel.RGB.G] - sample2[ColorModel.RGB.G]
                output[ColorModel.RGB.B] = sample1[ColorModel.RGB.B] - sample2[ColorModel.RGB.B]
                val delta = ColorUtil.sampleDistance(output, colorModel, sampleIndex, true)
                if (delta < 0) {
                    rgb[ColorModel.RGB.R] = min(1.0, -delta * sampleFactor)
                    rgb[ColorModel.RGB.G] = min(1.0, -delta * sampleFactor * 0.5)
                    rgb[ColorModel.RGB.B] = min(1.0, -delta * sampleFactor * 0.5)
                } else if (delta >= 0) {
                    rgb[ColorModel.RGB.R] = min(1.0, delta * sampleFactor * 0.5)
                    rgb[ColorModel.RGB.G] = min(1.0, delta * sampleFactor * 0.5)
                    rgb[ColorModel.RGB.B] = min(1.0, delta * sampleFactor)
                }
                deltaImage.setPixel(x, y, ColorModel.RGB, rgb)
            }
        }
    }

    private fun createEditor(): Node {
        val mainBox = HBox(SPACING)
        zoomInputImage = WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT)
        zoomInputDoubleImage = if (ACCURATE_PREVIEW) {
            WriteThroughArrayDoubleImage(JavaFXWritableDoubleImage(zoomInputImage), ColorModel.RGB)
        } else {
            JavaFXWritableDoubleImage(zoomInputImage)
        }
        zoomInputImageView = ImageView(zoomInputImage)
        zoomOutputImage = WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT)
        zoomOutputDoubleImage = WriteThroughArrayDoubleImage(JavaFXWritableDoubleImage(zoomOutputImage), ColorModel.RGB)
        zoomOutputImageView = ImageView(zoomOutputImage)
        zoomGradientImage = WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT)
        zoomGradientDoubleImage = WriteThroughArrayDoubleImage(JavaFXWritableDoubleImage(zoomGradientImage), ColorModel.RGB)
        zoomGradientImageView = ImageView(zoomGradientImage)
        zoomDeltaImage = WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT)
        zoomDeltaDoubleImage = JavaFXWritableDoubleImage(zoomDeltaImage)
        zoomDeltaImageView = ImageView(zoomDeltaImage)
        run {
            val mainGridPane = GridPane()
            mainGridPane.hgap = SPACING
            mainGridPane.vgap = SPACING
            mainBox.children.add(mainGridPane)
            var rowIndex = 0
            run {
                val sampleHBox = HBox(SPACING)
                mainGridPane.add(sampleHBox, 0, rowIndex, 4, 1)
                sampleHBox.children.add(Label("X:"))
                val zoomCenterXTextField = TextField()
                sampleHBox.children.add(zoomCenterXTextField)
                zoomCenterXTextField.prefWidth = 60.0
                Bindings.bindBidirectional(zoomCenterXTextField.textProperty(), zoomCenterXProperty, INTEGER_FORMAT)
                sampleHBox.children.add(Label("Y:"))
                val zoomCenterYTextField = TextField()
                sampleHBox.children.add(zoomCenterYTextField)
                zoomCenterYTextField.prefWidth = 60.0
                Bindings.bindBidirectional(zoomCenterYTextField.textProperty(), zoomCenterYProperty, INTEGER_FORMAT)
                sampleHBox.children.add(Label("Radius:"))
                sampleRadiusSpinner = Spinner(0, 30, 5)
                sampleHBox.children.add(sampleRadiusSpinner)
                sampleRadiusSpinner!!.tooltip = tooltip(("Radius of the sample area used to calculate the color of gradient fix points.\n"
                        + "The width and height of the sample area will be: 2*radius + 1"))
                sampleRadiusSpinner!!.styleClass.add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL)
                sampleRadiusSpinner!!.prefWidth = 70.0
                sampleRadiusProperty.bind(sampleRadiusSpinner!!.valueProperty())

                /*
        sampleHBox.getChildren().add(new Label("Pixel:"));
        Rectangle samplePixelRectangle = new Rectangle(COLOR_INDICATOR_SIZE, COLOR_INDICATOR_SIZE);
        sampleHBox.getChildren().add(samplePixelRectangle);
        samplePixelRectangle.fillProperty().bind(samplePixelColor);

        sampleHBox.getChildren().add(new Label("Avg:"));
        Rectangle sampleAverageRectangle = new Rectangle(COLOR_INDICATOR_SIZE, COLOR_INDICATOR_SIZE);
        sampleHBox.getChildren().add(sampleAverageRectangle);
        sampleAverageRectangle.fillProperty().bind(sampleAverageColor);

        sampleHBox.getChildren().add(new Label("Gradient:"));
        Rectangle sampleGradientRectangle = new Rectangle(COLOR_INDICATOR_SIZE, COLOR_INDICATOR_SIZE);
        sampleHBox.getChildren().add(sampleGradientRectangle);
        sampleGradientRectangle.fillProperty().bind(gradientPixelColor);
        */
                rowIndex++
            }
            run {
                run {
                    mainGridPane.add(Label("Zoom:"), 0, rowIndex)
                    mainGridPane.add(Label("Output Preview:"), 1, rowIndex)
                    rowIndex++
                    mainGridPane.add(withCrosshair(zoomInputImageView), 0, rowIndex)
                    mainGridPane.add(withCrosshair(zoomOutputImageView), 1, rowIndex)
                    rowIndex++
                }
                run {
                    mainGridPane.add(Label("Glow:"), 0, rowIndex)
                    val hbox = HBox(SPACING)
                    mainGridPane.add(hbox, 1, rowIndex)
                    hbox.children.add(Label("Delta:"))
                    val zoomDeltaColorModelComboBox = ComboBox(FXCollections
                            .observableArrayList(*SampleChannel.values()))
                    hbox.children.add(zoomDeltaColorModelComboBox)
                    tooltip(zoomDeltaColorModelComboBox, ("Color channel used to show the delta between the input image and glow image.\n"
                            + "The brightness delta is useful to determine how much color information is lost in the subtraction."))
                    Bindings.bindBidirectional(zoomDeltaColorModelComboBox.valueProperty(), zoomDeltaSampleChannelProperty)
                    val zoomDeltaSampleFactorSpinner = Spinner<Number>(1.0, 50.0, 20.0)
                    hbox.children.add(zoomDeltaSampleFactorSpinner)
                    tooltip(zoomDeltaSampleFactorSpinner, "Factor used to exaggerate the delta value.")
                    zoomDeltaSampleFactorSpinner.styleClass.add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL)
                    zoomDeltaSampleFactorSpinner.prefWidth = 70.0
                    zoomDeltaSampleFactorProperty.bind(zoomDeltaSampleFactorSpinner.valueProperty())
                    rowIndex++
                    mainGridPane.add(withCrosshair(zoomGradientImageView), 0, rowIndex)
                    mainGridPane.add(withCrosshair(zoomDeltaImageView), 1, rowIndex)
                    tooltip(zoomDeltaImageView, ("Shows the difference between the glow image and input image.\n"
                            + "The channel to calculate the difference can be selected: Red, Green, Blue, Hue, Saturation, Brightness\n"
                            + "Blue colors indicate a positive, red a negative difference."
                            + "If the delta channel is set to the 'Brightness', red colors indicate that the output image brightness will be < 0 and therefore information is lost."))
                    rowIndex++
                }
            }
            run {
                val glowTabPane = TabPane()
                mainGridPane.add(glowTabPane, 0, rowIndex, 4, 1)
                glowTabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                var glowSingleColorTab: Tab
                var glowBlurTab: Tab
                var glowGradientTab: Tab
                run {
                    val glowSingleColorGridPane = GridPane()
                    glowSingleColorGridPane.hgap = SPACING
                    glowSingleColorGridPane.vgap = SPACING
                    glowSingleColorTab = Tab("Single Color", glowSingleColorGridPane)
                    glowTabPane.tabs.add(glowSingleColorTab)
                    glowSingleColorTab.tooltip = tooltip(("Determine a single color that will be used uniformly to estimate the glow.\n"
                            + "This is a good strategy if the glow is uniform over the entire image."))
                    var glowSingleColorRowIndex = 0
                    run {
                        val buttonBox = HBox(SPACING)
                        glowSingleColorGridPane.add(buttonBox, 0, glowSingleColorRowIndex, 2, 1)
                        run {
                            val button = Button("Median All")
                            buttonBox.children.add(button)
                            tooltip(button, "Finds the median color of all pixels in the input image.")
                            button.onAction = EventHandler {
                                updateSingleGlowColor {
                                    if (medianAllColor == null) {
                                        medianAllColor = toJavafxColor(inputDoubleImage.medianPixel(ColorModel.RGB))
                                    }
                                    medianAllColor!!
                                }
                                singleGlowColorDescriptionProperty.set("Median All " + inputDoubleImage.width + "x" + inputDoubleImage.height)
                            }
                        }
                        run {
                            val button = Button("Average All")
                            buttonBox.children.add(button)
                            tooltip(button, "Finds the average color of all pixels in the input image.")
                            button.onAction = EventHandler {
                                updateSingleGlowColor {
                                    if (averageAllColor == null) {
                                        averageAllColor = toJavafxColor(inputDoubleImage.averagePixel(ColorModel.RGB))
                                    }
                                    averageAllColor!!
                                }
                                singleGlowColorDescriptionProperty.set("Average All " + inputDoubleImage.width + "x" + inputDoubleImage.height)
                            }
                        }
                        run {
                            val button: Button = Button("Darkest All")
                            buttonBox.children.add(button)
                            tooltip(button, "Finds the darkest color of all pixels in the input image.")
                            button.onAction = EventHandler {
                                updateSingleGlowColor {
                                    if (darkestAllColor == null) {
                                        darkestAllColor = toJavafxColor(inputDoubleImage.darkestPixel())
                                    }
                                    darkestAllColor!!
                                }
                                singleGlowColorDescriptionProperty.set("Darkest All " + inputDoubleImage.width + "x" + inputDoubleImage.height)
                            }
                        }
                        glowSingleColorRowIndex++
                    }
                    run {
                        val buttonBox = HBox(SPACING)
                        glowSingleColorGridPane.add(buttonBox, 0, glowSingleColorRowIndex, 2, 1)
                        run {
                            val button = Button("Median Zoom")
                            buttonBox.children.add(button)
                            tooltip(button, "Finds the median color of the pixels in the zoom input image.")
                            button.onAction = EventHandler {
                                updateSingleGlowColor { toJavafxColor(zoomInputDoubleImage.medianPixel()) }
                                val width = ZOOM_WIDTH
                                val height = ZOOM_HEIGHT
                                val x = zoomCenterXProperty.get() - width / 2
                                val y = zoomCenterYProperty.get() - height / 2
                                singleGlowColorDescriptionProperty.set("Median Zoom " + x + "," + y + " " + width + "x" + height)
                            }
                        }
                        run {
                            val button: Button = Button("Average Zoom")
                            buttonBox.children.add(button)
                            tooltip(button, "Finds the average color of the pixels in the zoom input image.")
                            button.onAction = EventHandler {
                                updateSingleGlowColor { toJavafxColor(zoomInputDoubleImage.averagePixel()) }
                                val width: Int = ZOOM_WIDTH
                                val height: Int = ZOOM_HEIGHT
                                val x: Int = zoomCenterXProperty.get() - width / 2
                                val y: Int = zoomCenterYProperty.get() - height / 2
                                singleGlowColorDescriptionProperty.set("Average Zoom " + x + "," + y + " " + width + "x" + height)
                            }
                        }
                        run {
                            val button: Button = Button("Darkest Zoom")
                            buttonBox.children.add(button)
                            tooltip(button, "Finds the darkest color of the pixels in the zoom input image.")
                            button.onAction = EventHandler {
                                updateSingleGlowColor { toJavafxColor(zoomInputDoubleImage.darkestPixel()) }
                                val width: Int = ZOOM_WIDTH
                                val height: Int = ZOOM_HEIGHT
                                val x: Int = zoomCenterXProperty.get() - width / 2
                                val y: Int = zoomCenterYProperty.get() - height / 2
                                singleGlowColorDescriptionProperty.set("Darkest Zoom " + x + "," + y + " " + width + "x" + height)
                            }
                        }
                        glowSingleColorRowIndex++
                    }
                    run {
                        val buttonBox = HBox(SPACING)
                        glowSingleColorGridPane.add(buttonBox, 0, glowSingleColorRowIndex, 2, 1)
                        run {
                            val button = Button("Median Sample")
                            buttonBox.children.add(button)
                            tooltip(button, "Finds the median color of the pixels in the sample radius of the zoom input image.")
                            button.onAction = EventHandler {
                                val zx = ZOOM_WIDTH / 2
                                val zy = ZOOM_HEIGHT / 2
                                val r = sampleRadiusProperty.get()
                                updateSingleGlowColor { toJavafxColor(zoomInputDoubleImage.croppedImage(zx - r, zy - r, r + r + 1, r + r + 1).medianPixel()) }
                                val width = r + r + 1
                                val height = r + r + 1
                                val x = zoomCenterXProperty.get() - width / 2
                                val y = zoomCenterYProperty.get() - height / 2
                                singleGlowColorDescriptionProperty.set("Median Sample " + x + "," + y + " " + width + "x" + height)
                            }
                        }
                        run {
                            val button: Button = Button("Average Sample")
                            buttonBox.children.add(button)
                            tooltip(button, "Finds the average color of the pixels in the sample radius of the zoom input image.")
                            button.onAction = EventHandler {
                                val zx: Int = ZOOM_WIDTH / 2
                                val zy: Int = ZOOM_HEIGHT / 2
                                val r: Int = sampleRadiusProperty.get()
                                updateSingleGlowColor { toJavafxColor(zoomInputDoubleImage.croppedImage(zx - r, zy - r, r + r + 1, r + r + 1).averagePixel()) }
                                val width: Int = r + r + 1
                                val height: Int = r + r + 1
                                val x: Int = zoomCenterXProperty.get() - width / 2
                                val y: Int = zoomCenterYProperty.get() - height / 2
                                singleGlowColorDescriptionProperty.set("Average Sample " + x + "," + y + " " + width + "x" + height)
                            }
                        }
                        run {
                            val button: Button = Button("Darkest Sample")
                            buttonBox.children.add(button)
                            tooltip(button, "Finds the darkest color of the pixels in the sample radius of the zoom input image.")
                            button.onAction = EventHandler {
                                val zx: Int = ZOOM_WIDTH / 2
                                val zy: Int = ZOOM_HEIGHT / 2
                                val r: Int = sampleRadiusProperty.get()
                                updateSingleGlowColor { toJavafxColor(zoomInputDoubleImage.croppedImage(zx - r, zy - r, r + r + 1, r + r + 1).darkestPixel()) }
                                val width: Int = r + r + 1
                                val height: Int = r + r + 1
                                val x: Int = zoomCenterXProperty.get() - width / 2
                                val y: Int = zoomCenterYProperty.get() - height / 2
                                singleGlowColorDescriptionProperty.set("Darkest Sample " + x + "," + y + " " + width + "x" + height)
                            }
                        }
                        glowSingleColorRowIndex++
                    }
                    run {
                        glowSingleColorGridPane.add(Label("Glow Color:"), 0, glowSingleColorRowIndex)
                        val sampleAverageRectangle = Rectangle(COLOR_INDICATOR_SIZE.toDouble(), COLOR_INDICATOR_SIZE.toDouble())
                        glowSingleColorGridPane.add(sampleAverageRectangle, 1, glowSingleColorRowIndex)
                        sampleAverageRectangle.fillProperty().bind(singleGlowColorProperty)
                        glowSingleColorRowIndex++
                    }
                    run {
                        glowSingleColorGridPane.add(Label("Glow Red:"), 0, glowSingleColorRowIndex)
                        val glowRedLabel = Label()
                        glowSingleColorGridPane.add(glowRedLabel, 1, glowSingleColorRowIndex)
                        singleGlowColorProperty.addListener { _, _, newValue: Color? -> glowRedLabel.text = PERCENT_FORMAT.format(newValue!!.red) }
                        glowSingleColorRowIndex++
                    }
                    run {
                        glowSingleColorGridPane.add(Label("Glow Green:"), 0, glowSingleColorRowIndex)
                        val glowGreenLabel = Label()
                        glowSingleColorGridPane.add(glowGreenLabel, 1, glowSingleColorRowIndex)
                        singleGlowColorProperty.addListener { _, _, newValue: Color? -> glowGreenLabel.text = PERCENT_FORMAT.format(newValue!!.green) }
                        glowSingleColorRowIndex++
                    }
                    run {
                        glowSingleColorGridPane.add(Label("Glow Blue:"), 0, glowSingleColorRowIndex)
                        val glowBlueLabel = Label()
                        glowSingleColorGridPane.add(glowBlueLabel, 1, glowSingleColorRowIndex)
                        singleGlowColorProperty.addListener { _, _, newValue: Color? -> glowBlueLabel.text = PERCENT_FORMAT.format(newValue!!.blue) }
                        glowSingleColorRowIndex++
                    }
                    run {
                        glowSingleColorGridPane.add(Label("Color Description:"), 0, glowSingleColorRowIndex)
                        val glowColorDescriptionLabel = Label()
                        glowSingleColorGridPane.add(glowColorDescriptionLabel, 1, glowSingleColorRowIndex)
                        glowColorDescriptionLabel.textProperty().bind(singleGlowColorDescriptionProperty)
                        glowSingleColorRowIndex++
                    }
                }
                run {
                    val glowBlurGridPane = GridPane()
                    glowBlurGridPane.hgap = SPACING
                    glowBlurGridPane.vgap = SPACING
                    glowBlurTab = Tab("Blur", glowBlurGridPane)
                    glowTabPane.tabs.add(glowBlurTab)
                    glowBlurTab.tooltip = tooltip(("Blurs the input image to calculate the glow image.\n"
                            + "This is a good strategy for images where the object of interest occupies only a small area."))
                    var glowBlurRowIndex = 0
                    run {
                        glowBlurGridPane.add(Label("Despeckle Radius:"), 0, glowBlurRowIndex)
                        val despeckleRadiusTextField = TextField()
                        glowBlurGridPane.add(despeckleRadiusTextField, 1, glowBlurRowIndex)
                        Bindings.bindBidirectional(despeckleRadiusTextField.textProperty(), despeckleRadiusProperty, INTEGER_FORMAT)
                        glowBlurRowIndex++

                        glowBlurGridPane.add(Label("Blur Radius:"), 0, glowBlurRowIndex)
                        val blurRadiusTextField = TextField()
                        glowBlurGridPane.add(blurRadiusTextField, 1, glowBlurRowIndex)
                        Bindings.bindBidirectional(blurRadiusTextField.textProperty(), blurRadiusProperty, INTEGER_FORMAT)
                        glowBlurRowIndex++
                    }
                }
                run {
                    val glowInterpolateGridPane = GridPane()
                    glowInterpolateGridPane.hgap = SPACING
                    glowInterpolateGridPane.vgap = SPACING
                    glowGradientTab = Tab("Gradient", glowInterpolateGridPane)
                    glowTabPane.tabs.add(glowGradientTab)
                    glowGradientTab.tooltip = tooltip(("Interpolates the glow between two or more points.\n"
                            + "This is a good strategy for images with a glow that shows a strong gradient."))
                    var glowInterpolateRowIndex = 0
                    run {
                        val fixPointToolbar = HBox(SPACING)
                        glowInterpolateGridPane.add(fixPointToolbar, 0, glowInterpolateRowIndex, 4, 1)
                        val addFixPointButton = Button("Add")
                        fixPointToolbar.children.add(addFixPointButton)
                        tooltip(addFixPointButton, ("Adds a new fix point to interpolate the glow.\n"
                                + "Make sure to set points in areas that only contain background glow and no nebula or stars.\n"
                                + "It is best to define an odd number of points - three points is usually a good number."))
                        addFixPointButton.onAction = EventHandler {
                            val x = zoomCenterXProperty.get()
                            val y = zoomCenterYProperty.get()
                            addFixPoint(x, y)
                        }
                        val clearFixPointButton = Button("Clear")
                        fixPointToolbar.children.add(clearFixPointButton)
                        clearFixPointButton.onAction = EventHandler { fixPoints.clear() }
                        glowInterpolateRowIndex++
                    }
                    run {
                        val fixPointTableView = TableView(fixPoints)
                        glowInterpolateGridPane.add(fixPointTableView, 0, glowInterpolateRowIndex, 4, 1)
                        fixPointTableView.placeholder = Label("Add points to define the background gradient.")
                        fixPointTableView.prefHeight = 100.0
                        fixPointTableView.setRowFactory {
                            val tableRow = TableRow<FixPoint>()
                            val gotoMenuItem = MenuItem("Go To")
                            gotoMenuItem.onAction = EventHandler { setZoom(tableRow.item.x, tableRow.item.y) }
                            val removeMenuItem = MenuItem("Remove")
                            removeMenuItem.onAction = EventHandler { fixPoints.remove(tableRow.item) }
                            tableRow.contextMenu = ContextMenu(
                                    gotoMenuItem,
                                    removeMenuItem
                            )
                            tableRow
                        }
                        addTableColumn(fixPointTableView, "X", 40.0) { fixPoint: FixPoint -> ReadOnlyIntegerWrapper(fixPoint.x) }
                        addTableColumn(fixPointTableView, "Y", 40.0) { fixPoint: FixPoint -> ReadOnlyIntegerWrapper(fixPoint.y) }
                        addTableColumn(fixPointTableView, "Color", 50.0) { fixPoint: FixPoint ->
                            val rectangle = Rectangle(COLOR_INDICATOR_SIZE.toDouble(), COLOR_INDICATOR_SIZE.toDouble())
                            rectangle.fill = fixPoint.color
                            ReadOnlyObjectWrapper(rectangle)
                        }
                        addTableColumn(fixPointTableView, "Red", 70.0) { fixPoint: FixPoint -> ReadOnlyStringWrapper(PERCENT_FORMAT.format(fixPoint.color.red)) }
                        addTableColumn(fixPointTableView, "Green", 70.0) { fixPoint: FixPoint -> ReadOnlyStringWrapper(PERCENT_FORMAT.format(fixPoint.color.green)) }
                        addTableColumn(fixPointTableView, "Blue", 70.0) { fixPoint: FixPoint -> ReadOnlyStringWrapper(PERCENT_FORMAT.format(fixPoint.color.blue)) }
                        glowInterpolateRowIndex++
                    }
                    run {
                        run {
                            glowInterpolateGridPane.add(Label("Point Finder:"), 0, glowInterpolateRowIndex)
                            val pointFinderComboBox = ComboBox(FXCollections
                                    .observableArrayList(*PointFinderStrategy.values()))
                            glowInterpolateGridPane.add(pointFinderComboBox, 1, glowInterpolateRowIndex)
                            Bindings.bindBidirectional(pointFinderComboBox.valueProperty(), pointFinderStrategyProperty)
                            glowInterpolateRowIndex++
                        }
                        run {
                            glowInterpolateGridPane.add(Label("Interpolation Power:"), 0, glowInterpolateRowIndex)
                            val interpolationPowerTextField = TextField()
                            glowInterpolateGridPane.add(interpolationPowerTextField, 1, glowInterpolateRowIndex)
                            Bindings.bindBidirectional(interpolationPowerTextField.textProperty(),
                                    interpolationPowerProperty, DOUBLE_FORMAT)
                            glowInterpolateRowIndex++
                        }
                    }
                }
                glowTabPane.selectionModel.selectedItemProperty().addListener { _, _, newValue: Tab ->
                    when {
                        newValue === glowSingleColorTab -> {
                            glowStrategyProperty.set(GlowStrategy.SingleColor)
                        }
                        newValue === glowBlurTab -> {
                            glowStrategyProperty.set(GlowStrategy.Blur)
                        }
                        newValue === glowGradientTab -> {
                            glowStrategyProperty.set(GlowStrategy.Gradient)
                        }
                    }
                }
                glowStrategyProperty.addListener { _, _, newValue: GlowStrategy? ->
                    when (newValue) {
                        GlowStrategy.SingleColor -> glowTabPane.selectionModel.select(glowSingleColorTab)
                        GlowStrategy.Blur -> glowTabPane.selectionModel.select(glowBlurTab)
                        GlowStrategy.Gradient -> glowTabPane.selectionModel.select(glowGradientTab)
                    }
                }
                glowTabPane.selectionModel.select(glowGradientTab)
            }
            run {
                val subtractionGridPane = GridPane()
                subtractionGridPane.hgap = SPACING
                subtractionGridPane.vgap = SPACING
                mainBox.children.add(subtractionGridPane)
                var algorithmRowIndex = 0
                run {
                    subtractionGridPane.add(Label("Removal:"), 0, algorithmRowIndex)
                    val removalFactorTextField = TextField()
                    subtractionGridPane.add(removalFactorTextField, 1, algorithmRowIndex)
                    Bindings.bindBidirectional(removalFactorTextField.textProperty(), removalFactorProperty, PERCENT_FORMAT)
                    algorithmRowIndex++
                }
                run {
                    subtractionGridPane.add(Label("Sample Subtraction:"), 0, algorithmRowIndex)
                    val sampleSubtractionComboBox = ComboBox(FXCollections
                            .observableArrayList(*SubtractionStrategy.values()))
                    subtractionGridPane.add(sampleSubtractionComboBox, 1, algorithmRowIndex)
                    tooltip(sampleSubtractionComboBox, ("Different strategies to subtract the calculated glow from the input image.\n"
                            + "Subtract: Simply subtracts the RGB values of the glow.\n"
                            + "Subtract Linear: Subtracts the RGB values of the glow and corrects the remaining value linearly.\n"
                            + "Spline 1%: Uses a spline function to reduce the RGB value of the glow to 1%.\n"
                            + "Spline 1% + Stretch: Uses a spline function to reduce the RGB value of the glow to 1% - stretching the remaining value non-linearly.\n"
                            + "Spline 10%: Uses a spline function to reduce the RGB value of the glow to 10%.\n"))
                    Bindings.bindBidirectional(sampleSubtractionComboBox.valueProperty(), sampleSubtractionStrategyProperty)
                    algorithmRowIndex++
                }
                run {
                    subtractionGridPane.add(Label("Curve:"), 0, algorithmRowIndex)
                    colorCurveCanvas = Canvas(COLOR_CURVE_WIDTH.toDouble(), COLOR_CURVE_HEIGHT.toDouble())
                    subtractionGridPane.add(colorCurveCanvas, 1, algorithmRowIndex)
                    tooltip(colorCurveCanvas, "Color curve shows how the RGB values for the current pixel from the input image (x-axis) to the output image (y-axis) are transformed.")
                    algorithmRowIndex++
                    subtractionGridPane.add(Label("Input:"), 0, algorithmRowIndex)
                    inputHistogramCanvas = Canvas(HISTOGRAM_WIDTH.toDouble(), HISTOGRAM_HEIGHT.toDouble())
                    subtractionGridPane.add(inputHistogramCanvas, 1, algorithmRowIndex)
                    tooltip(inputHistogramCanvas, "Histogram of the RGB values over the entire input image.")
                    algorithmRowIndex++
                    subtractionGridPane.add(Label("Zoom Input:"), 0, algorithmRowIndex)
                    zoomInputHistogramCanvas = Canvas(HISTOGRAM_WIDTH.toDouble(), HISTOGRAM_HEIGHT.toDouble())
                    subtractionGridPane.add(zoomInputHistogramCanvas, 1, algorithmRowIndex)
                    tooltip(zoomInputHistogramCanvas, "Histogram of the RGB values over the zoom input image.")
                    algorithmRowIndex++
                    subtractionGridPane.add(Label("Zoom Output:"), 0, algorithmRowIndex)
                    zoomOutputHistogramCanvas = Canvas(HISTOGRAM_WIDTH.toDouble(), HISTOGRAM_HEIGHT.toDouble())
                    subtractionGridPane.add(zoomOutputHistogramCanvas, 1, algorithmRowIndex)
                    tooltip(zoomOutputHistogramCanvas, "Histogram of the RGB values over the zoom output image.")
                    algorithmRowIndex++
                }
            }
        }
        setupZoomDragEvents(zoomInputImageView!!)
        setupZoomDragEvents(zoomOutputImageView!!)
        setupZoomDragEvents(zoomGradientImageView!!)
        setupZoomDragEvents(zoomDeltaImageView!!)
        return mainBox
    }

    private fun toJavafxColor(color: DoubleArray): Color {
        return Color(
                color[ColorModel.RGB.R],
                color[ColorModel.RGB.G],
                color[ColorModel.RGB.B],
                1.0)
    }

    private fun toDoubleColorRGB(color: Color): DoubleArray {
        return doubleArrayOf(color.red, color.green, color.blue)
    }

    private fun updateSingleGlowColor(producer: Supplier<Color>) {
        Thread {
            singleGlowColorUpdate.getAndSet(producer.get())
            Platform.runLater { singleGlowColorProperty.set(singleGlowColorUpdate.getAndSet(null)) }
        }.start()
    }

    private fun addFixPoint(x: Int, y: Int) {
        val sampleRadius = sampleRadiusProperty.get()
        val color = inputDoubleImage.averagePixel(
                x - sampleRadius,
                y - sampleRadius,
                sampleRadius + sampleRadius + 1,
                sampleRadius + sampleRadius + 1)
        fixPoints.add(FixPoint(x, y, Color(color[0], color[1], color[2], 1.0)))
    }

    private fun withZoomRectangle(imageView: ImageView, zoomRectanglePane: Pane): Node {
        val rectangle = Rectangle()
        rectangle.isMouseTransparent = true
        rectangle.strokeProperty().bind(crosshairColorProperty)
        rectangle.fill = Color.TRANSPARENT
        zoomCenterXProperty.addListener { _, _, _ -> updateZoomRectangle(rectangle) }
        zoomCenterYProperty.addListener { _, _, _ -> updateZoomRectangle(rectangle) }
        inputImageView!!.imageProperty().addListener { _, _, _ -> updateZoomRectangle(rectangle) }
        return Pane(imageView, rectangle, zoomRectanglePane)
    }

    private fun updateZoomRectangle(rectangle: Rectangle) {
        val width = ZOOM_WIDTH / inputImage.width * inputImageView!!.boundsInLocal.width
        val height = ZOOM_HEIGHT / inputImage.height * inputImageView!!.boundsInLocal.height
        val x = zoomCenterXProperty.get() / inputImage.width * inputImageView!!.boundsInLocal.width
        val y = zoomCenterYProperty.get() / inputImage.height * inputImageView!!.boundsInLocal.height
        rectangle.x = x - width / 2
        rectangle.y = y - height / 2
        rectangle.width = width
        rectangle.height = height
    }

    private fun withCrosshair(imageView: ImageView?): Node {
        val size = sampleRadiusProperty.multiply(2).add(1)
        val rectangle = Rectangle()
        rectangle.isMouseTransparent = true
        rectangle.widthProperty().bind(size)
        rectangle.heightProperty().bind(size)
        rectangle.strokeProperty().bind(crosshairColorProperty)
        rectangle.fill = Color.TRANSPARENT
        rectangle.x = (ZOOM_WIDTH / 2).toDouble()
        rectangle.y = (ZOOM_HEIGHT / 2).toDouble()
        return StackPane(imageView, rectangle)
    }

    private fun <E, V> addTableColumn(tableView: TableView<E>, header: String, prefWidth: Double, valueFunction: Function<E, ObservableValue<V>>): TableColumn<E, V> {
        val column = TableColumn<E, V>(header)
        column.prefWidth = prefWidth
        column.setCellValueFactory { cellData -> valueFunction.apply(cellData.value) }
        tableView.columns.add(column)
        return column
    }

    private fun <E, V> addTableColumn(tableView: TableView<E>, header: String, prefWidth: Double, valueFunction: Function<E, ObservableValue<V>>, nodeFunction: Function<V, Node>): TableColumn<E, V> {
        val column = TableColumn<E, V>(header)
        column.prefWidth = prefWidth
        column.setCellValueFactory { cellData -> valueFunction.apply(cellData.value) }
        column.setCellFactory {
            object : TableCell<E, V>() {
                override fun updateItem(item: V, empty: Boolean) {
                    if (empty) {
                        setGraphic(null)
                    } else {
                        setGraphic(nodeFunction.apply(item))
                    }
                    super.updateItem(item, empty)
                }
            }
        }
        tableView.columns.add(column)
        return column
    }

    private var zoomDragX: Double? = null
    private var zoomDragY: Double? = null
    private fun setupZoomDragEvents(imageView: ImageView) {
        imageView.onMousePressed = EventHandler { event: MouseEvent ->
            zoomDragX = event.x
            zoomDragY = event.y
        }
        imageView.onMouseDragged = EventHandler { event: MouseEvent ->
            val deltaX = zoomDragX!! - event.x
            val deltaY = zoomDragY!! - event.y
            zoomDragX = event.x
            zoomDragY = event.y
            var zoomX = zoomCenterXProperty.get() + deltaX.toInt()
            var zoomY = zoomCenterYProperty.get() + deltaY.toInt()
            zoomX = max(zoomX, 0)
            zoomY = max(zoomY, 0)
            zoomX = min(zoomX, inputImage.width.toInt() - 1)
            zoomY = min(zoomY, inputImage.height.toInt() - 1)
            zoomCenterXProperty.set(zoomX)
            zoomCenterYProperty.set(zoomY)
            updateZoom(zoomX, zoomY)
        }
        imageView.onMouseDragReleased = EventHandler {
            zoomDragX = null
            zoomDragY = null
        }
    }

    private fun setMouseDragEvents(node: Node, handler: EventHandler<in MouseEvent>) {
        node.onMouseClicked = handler
        node.onMouseDragged = handler
        node.onMouseReleased = handler
    }

    private fun tooltip(node: Node?, text: String) {
        Tooltip.install(node, tooltip(text))
    }

    private fun tooltip(text: String): Tooltip {
        return Tooltip(text)
    }

    private fun homeDirectory(): Path {
        //path.toFile().mkdirs();
        return Paths.get(System.getProperty("user.home", "."))
    }

    private class FixPoint(val x: Int, val y: Int, val color: Color)
    companion object {
        private const val IMAGE_WIDTH = 600
        private const val IMAGE_HEIGHT = 600
        private const val ZOOM_WIDTH = 150
        private const val ZOOM_HEIGHT = 150
        private const val HISTOGRAM_WIDTH = 150
        private const val HISTOGRAM_HEIGHT = 50
        private const val COLOR_CURVE_WIDTH = 150
        private const val COLOR_CURVE_HEIGHT = 150
        private const val COLOR_INDICATOR_SIZE = 15
        private const val SPACING = 2.0
        private const val ACCURATE_PREVIEW = true
        private val INTEGER_FORMAT = DecimalFormat("##0")
        private val DOUBLE_FORMAT = DecimalFormat("##0.000")
        private val PERCENT_FORMAT = DecimalFormat("##0.000%")
        private const val EZ_ASTRO_FILE_EXTENSION = ".ezastro"
        private val RED_SEMI = Color(1.0, 0.0, 0.0, 0.8)
        private val GREEN_SEMI = Color(0.0, 1.0, 0.0, 0.8)
        private val BLUE_SEMI = Color(0.0, 0.0, 1.0, 0.8)

        private val DummyWritableImage = WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT)
        private val DummyDoubleImage = ArrayDoubleImage(ZOOM_WIDTH, ZOOM_HEIGHT, ColorModel.RGB)


        @JvmStatic
        fun main(args: Array<String>) {
            launch(AstrophotographyApp::class.java)
        }
    }
}