package ch.obermuhlner.astro.javafx.stack

import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.ImageQuality
import ch.obermuhlner.astro.image.ImageReader
import ch.obermuhlner.astro.image.MemoryMappedFileDoubleImage
import ch.obermuhlner.astro.image.color.ColorModel
import ch.obermuhlner.astro.image.color.ColorUtil
import ch.obermuhlner.astro.javafx.*
import ch.obermuhlner.astro.javafx.glow.GlowRemovalApp
import ch.obermuhlner.astro.stack.AverageStacker
import ch.obermuhlner.astro.stack.MaxStacker
import ch.obermuhlner.astro.stack.MedianStacker
import ch.obermuhlner.astro.stack.Stacker
import ch.obermuhlner.astro.stack.StackingImage
import javafx.application.Application
import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseEvent
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.*
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ImageStackerApp : Application() {

    private val stackingFiles = FXCollections.observableArrayList<StackingFile>()
    private var selectedStackingFile: StackingFile? = null
    private var baseImage: DoubleImage? = null

    private val zoomBaseImage: WritableImage = WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT)
    private val zoomBaseDoubleImage: DoubleImage = JavaFXWritableDoubleImage(zoomBaseImage)

    private val zoomStackingImage: WritableImage = WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT)
    private val zoomStackingDoubleImage: DoubleImage = JavaFXWritableDoubleImage(zoomStackingImage)

    private val zoomDiffImage: WritableImage = WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT)
    private val zoomDiffDoubleImage: DoubleImage = JavaFXWritableDoubleImage(zoomDiffImage)

    private val zoomOutputImage: WritableImage = WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT)
    private val zoomOutputDoubleImage: DoubleImage = JavaFXWritableDoubleImage(zoomOutputImage)

    private val zoomCenterXProperty: IntegerProperty = SimpleIntegerProperty()
    private val zoomCenterYProperty: IntegerProperty = SimpleIntegerProperty()

    private val stackerStrategyProperty: ObjectProperty<StackingStrategy> = SimpleObjectProperty(StackingStrategy.Average)

    private val zoomAverageErrorProperty: DoubleProperty = SimpleDoubleProperty()

    private val baseImageView = ImageView()
    private val zoomBaseImageView = ImageView()
    private val zoomStackingImageView = ImageView()
    private val zoomDiffImageView = ImageView()
    private val zoomOutputImageView = ImageView()

    fun show() {
        start(Stage())
    }

    override fun start(primaryStage: Stage) {
        val root = Group()
        val scene = Scene(root)

        root.children += borderpane {
            top = createToolbar(primaryStage)
            center = createImageViewer()
            right = createEditor()
        }

        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun createToolbar(stage: Stage): Node {
        return hbox(SPACING) {
            children += button("Open Images ...") {
                onAction = EventHandler {
                    openImageFiles(stage)
                }
            }
            children += button("Open Project ...") {
                onAction = EventHandler {
                    loadProject(File("Untitled.ezastacker"))
                }
            }
            children += button("Save Project ...") {
                onAction = EventHandler {
                    saveProject(File("Untitled.ezastacker"))
                }
            }
        }
    }

    private fun openImageFiles(stage: Stage) {
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = homeDirectory().toFile()
        fileChooser.title = "Open Input Images"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image", "*.tif", "*.tiff", "*.png", "*.jpg", "*.jpeg"))
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("All", "*"))
        val chosenFiles = fileChooser.showOpenMultipleDialog(stage)
        if (chosenFiles != null) {
            for (chosenFile in chosenFiles) {
                val base = stackingFiles.isEmpty()
                val stackingFile = StackingFile(chosenFile, base)
                stackingFiles.add(stackingFile)
                if (base) {
                    updateBaseStackingFile(stackingFile)
                } else {
                    updateSelectedStackingFile(stackingFile)
                }
            }
        }
    }

    private fun createImageViewer(): Node {
        baseImageView.isPreserveRatio = true
        baseImageView.fitWidth = IMAGE_WIDTH.toDouble()
        baseImageView.fitHeight = IMAGE_HEIGHT.toDouble()

        setupImageSelectionListener(baseImageView)

        return baseImageView
    }

    private fun createEditor(): Node {
        return gridpane {
            row {
                cell(4, 1) {
                    tableview(stackingFiles) {
                        prefHeight = 150.0

                        column("Path", { stacking: StackingFile -> ReadOnlyStringWrapper(stacking.file.path) }) {
                            prefWidth = 200.0
                        }
                        column("File", { stacking: StackingFile -> ReadOnlyStringWrapper(stacking.file.name) }) {
                            prefWidth = 100.0
                        }
                        column("X", { stacking: StackingFile -> stacking.xProperty }) {
                            prefWidth = 40.0
                        }
                        column("Y", { stacking: StackingFile -> stacking.yProperty }) {
                            prefWidth = 40.0
                        }
                        column("Base", { stacking: StackingFile -> stacking.baseProperty }) {
                            prefWidth = 40.0
                        }
                        column("Stack", { stacking: StackingFile -> stacking.stackProperty }) {
                            prefWidth = 40.0
                        }
                        column("Score", { stacking: StackingFile -> stacking.scoreProperty }) {
                            prefWidth = 50.0
                        }

                        selectionModel.selectedItemProperty().addListener { _, _, selected -> updateSelectedStackingFile(selected) }
                    }
                }
            }
            row {
                cell {
                    label("Zoom Base:")
                }
                cell {
                    label("Zoom Stacking:")
                }
            }
            row {
                cell {
                    node(zoomBaseImageView) {
                        image = zoomBaseImage
                        setupZoomDragEvents(this)
                    }
                }
                cell {
                    node(zoomStackingImageView) {
                        image = zoomStackingImage
                        setupStackingDragEvents(this)
                    }
                }
                cell {
                    hbox {
                        children += button("^") {
                            onAction = EventHandler {
                                selectedStackingFile?.let {
                                    it.yProperty.set(it.yProperty.get() + 1)
                                    updateZoom()
                                }
                            }
                        }
                        children += button("V") {
                            onAction = EventHandler {
                                selectedStackingFile?.let {
                                    it.yProperty.set(it.yProperty.get() - 1)
                                    updateZoom()
                                }
                            }
                        }
                        children += button("<") {
                            onAction = EventHandler {
                                selectedStackingFile?.let {
                                    it.xProperty.set(it.xProperty.get() + 1)
                                    updateZoom()
                                }
                            }
                        }
                        children += button(">") {
                            onAction = EventHandler {
                                selectedStackingFile?.let {
                                    it.xProperty.set(it.xProperty.get() - 1)
                                    updateZoom()
                                }
                            }
                        }
                    }
                }
            }
            row {
                cell {
                    label("Diff:")
                }
                cell {
                    hbox(SPACING) {
                        children += label("Stacked:")
                        children += combobox(StackingStrategy.values()) {
                            Bindings.bindBidirectional(valueProperty(), stackerStrategyProperty)

                            stackerStrategyProperty.addListener { _, _, _ -> updateZoomOutputImage() }
                        }
                    }
                }
            }
            row {
                cell {
                    node(zoomDiffImageView) {
                        image = zoomDiffImage
                        setupStackingDragEvents(this)
                    }
                }
                cell {
                    node(zoomOutputImageView) {
                        image = zoomOutputImage
                        setupZoomDragEvents(this)
                    }
                }
                cell {
                    label("Average Error:")
                }
                cell {
                    label {
                        Bindings.bindBidirectional(textProperty(), zoomAverageErrorProperty, PERCENT_FORMAT)
                    }
                }
            }
        }
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

    private fun setMouseDragEvents(node: Node, handler: EventHandler<in MouseEvent>) {
        node.onMouseClicked = handler
        node.onMouseDragged = handler
        node.onMouseReleased = handler
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
            zoomX = min(zoomX, baseImage?.width ?: 1 - 1)
            zoomY = min(zoomY, baseImage?.height ?: 1 - 1)
            zoomCenterXProperty.set(zoomX)
            zoomCenterYProperty.set(zoomY)
            updateZoom(zoomX, zoomY)
        }
        imageView.onMouseDragReleased = EventHandler {
            zoomDragX = null
            zoomDragY = null
        }
    }

    private var stackingDragX: Double? = null
    private var stackingDragY: Double? = null
    private fun setupStackingDragEvents(imageView: ImageView) {
        imageView.onMousePressed = EventHandler { event: MouseEvent ->
            stackingDragX = event.x
            stackingDragY = event.y
        }
        imageView.onMouseDragged = EventHandler { event: MouseEvent ->
            val selectedStacking = selectedStackingFile
            if (selectedStacking != null && stackingDragX != null && stackingDragY != null) {
                val deltaX = stackingDragX!! - event.x
                val deltaY = stackingDragY!! - event.y
                stackingDragX = event.x
                stackingDragY = event.y
                var stackingX = selectedStacking.xProperty.get() + deltaX.toInt()
                var stackingY = selectedStacking.yProperty.get() + deltaY.toInt()
                selectedStacking.xProperty.set(stackingX)
                selectedStacking.yProperty.set(stackingY)
                updateZoom()
            }
        }
        imageView.onMouseDragReleased = EventHandler {
            stackingDragX = null
            stackingDragY = null
        }
    }

    private fun updateBaseStackingFile(stackingFile: StackingFile) {
        val image = loadImage(stackingFile.file)
        stackingFile.image = image

        updateBaseImage(image)
    }

    private fun updateBaseImage(image: DoubleImage) {
        baseImage = image;
        baseImageView.image = JavaFXImageUtil.createWritableImage(image)

        zoomCenterXProperty.set(image.width / 2)
        zoomCenterYProperty.set(image.height / 2)
    }

    private fun updateSelectedStackingFile(stackingFile: StackingFile?) {
        if (stackingFile != null) {
            if (stackingFile.image == null) {
                stackingFile.image = loadImage(stackingFile.file)
            }
            selectedStackingFile = stackingFile
        }
        updateZoom()
    }

    private fun setZoom(x: Int, y: Int) {
        zoomCenterXProperty.set(x)
        zoomCenterYProperty.set(y)
        updateZoom(x, y)
    }

    private fun updateZoom(zoomX: Int = zoomCenterXProperty.get(), zoomY: Int = zoomCenterYProperty.get()) {
        val zoomOffsetX = zoomX - ZOOM_WIDTH / 2
        val zoomOffsetY = zoomY - ZOOM_HEIGHT / 2

        val base = baseImage
        if (base == null) {
            return
        }
        val croppedBaseImage = base.croppedImage(zoomOffsetX, zoomOffsetY, ZOOM_WIDTH, ZOOM_HEIGHT)
        zoomBaseDoubleImage.setPixels(croppedBaseImage)

        val selectedFile = selectedStackingFile
        if (selectedFile == null || selectedFile.image == null) {
            zoomDiffDoubleImage.setPixels(croppedBaseImage)
        } else {
            val croppedStackingImage = selectedFile.image!!.croppedImage(zoomOffsetX + selectedFile.xProperty.get(), zoomOffsetY + selectedFile.yProperty.get(), ZOOM_WIDTH, ZOOM_HEIGHT)
            zoomStackingDoubleImage.setPixels(croppedStackingImage)

            val averageError = updateZoomDiffImage(croppedBaseImage, croppedStackingImage, zoomDiffDoubleImage)
            zoomAverageErrorProperty.set(averageError)
        }
        updateZoomOutputImage()
    }

    private fun updateZoomDiffImage(baseImage: DoubleImage, stackingImage: DoubleImage, diffImage: DoubleImage): Double {
        val sampleFactor = 5

        val baseColor = DoubleArray(3)
        val stackingColor = DoubleArray(3)
        val outputColor = DoubleArray(3)

        val n = baseImage.width * baseImage.height
        var error = 0.0

        for (y in 0 until baseImage.height) {
            for (x in 0 until baseImage.width) {
                baseImage.getPixel(x, y, ColorModel.RGB, baseColor)
                stackingImage.getPixel(x, y, ColorModel.RGB, stackingColor)

                outputColor[ColorModel.RGB.R] = baseColor[ColorModel.RGB.R] - stackingColor[ColorModel.RGB.R]
                outputColor[ColorModel.RGB.G] = baseColor[ColorModel.RGB.G] - stackingColor[ColorModel.RGB.G]
                outputColor[ColorModel.RGB.B] = baseColor[ColorModel.RGB.B] - stackingColor[ColorModel.RGB.B]

                val delta = ColorUtil.sampleDistance(outputColor, ColorModel.HSV, 2, true)
                error += delta * delta
                if (delta < 0) {
                    outputColor[ColorModel.RGB.R] = min(1.0, -delta * sampleFactor)
                    outputColor[ColorModel.RGB.G] = min(1.0, -delta * sampleFactor * 0.5)
                    outputColor[ColorModel.RGB.B] = min(1.0, -delta * sampleFactor * 0.5)
                } else if (delta >= 0) {
                    outputColor[ColorModel.RGB.R] = min(1.0, delta * sampleFactor * 0.5)
                    outputColor[ColorModel.RGB.G] = min(1.0, delta * sampleFactor * 0.5)
                    outputColor[ColorModel.RGB.B] = min(1.0, delta * sampleFactor)
                }

                diffImage.setPixel(x, y, ColorModel.RGB, outputColor)
            }
        }
        error /= n.toDouble()
        return error
    }

    private fun updateZoomOutputImage() {
        val zx = zoomCenterXProperty.get() - ZOOM_WIDTH/2
        val zy = zoomCenterYProperty.get() - ZOOM_HEIGHT/2

        val stackingImages = stackingFiles
                .filter { s -> s.stackProperty.get() && s.image != null }
                .map { s -> StackingImage(s.image!!.croppedImage(zx+s.xProperty.get(), zy+s.yProperty.get(), ZOOM_WIDTH, ZOOM_HEIGHT, false), 0, 0) }

        val stacker = stackerStrategyProperty.get().stacker

        stacker.stack(stackingImages, zoomOutputDoubleImage)
    }

    private fun calculateStackedColor(x: Int, y: Int, stackingFiles: List<StackingFile>, color: DoubleArray): DoubleArray {
        color[ColorModel.RGB.R] = 0.0
        color[ColorModel.RGB.G] = 0.0
        color[ColorModel.RGB.B] = 0.0

        val tempColor = DoubleArray(3)
        var n = 0

        for (stackingFile in stackingFiles) {
            if (stackingFile.baseProperty.get() || stackingFile.stackProperty.get()) {
                if (stackingFile.image != null) {
                    stackingFile.image!!.getPixel(x+stackingFile.xProperty.get(), y+stackingFile.yProperty.get(), ColorModel.RGB, tempColor)
                    color[ColorModel.RGB.R] += tempColor[ColorModel.RGB.R]
                    color[ColorModel.RGB.G] += tempColor[ColorModel.RGB.G]
                    color[ColorModel.RGB.B] += tempColor[ColorModel.RGB.B]
                    n++;
                }
            }
        }

        color[ColorModel.RGB.R] /= n.toDouble()
        color[ColorModel.RGB.G] /= n.toDouble()
        color[ColorModel.RGB.B] /= n.toDouble()
        return color
    }

    @Throws(IOException::class)
    private fun loadImage(file: File): DoubleImage {
        val mmiFile = File(file.path + ".mmi")
        if (mmiFile.exists()) {
            return MemoryMappedFileDoubleImage.fromFile(mmiFile)
        } else {
            val image = ImageReader.read(file, ImageQuality.High)
            return MemoryMappedFileDoubleImage.fromImage(image, mmiFile)
        }
    }

    private fun saveProject(file: File) {
        PrintWriter(FileWriter(file)).use { writer ->
            val properties = Properties()
            properties["version"] = "0.0.1"
            for (i in stackingFiles.indices) {
                properties["stacking.$i.file"] = stackingFiles[i].file.path
                properties["stacking.$i.x"] = stackingFiles[i].xProperty.get().toString()
                properties["stacking.$i.y"] = stackingFiles[i].yProperty.get().toString()
                properties["stacking.$i.base"] = stackingFiles[i].baseProperty.get().toString()
                properties["stacking.$i.stack"] = stackingFiles[i].stackProperty.get().toString()
            }
            properties.store(writer, "EZ-Astrophotography Image Stacker\nhttps://github.com/eobermuhlner/ez-astrophotography")
        }
    }

    private fun loadProject(file: File) {
        FileReader(file).use { reader ->
            val properties = Properties()
            properties.load(reader)
            val version = properties.getProperty("version")
            if (!version.startsWith("0.")) {
                throw IOException("Incompatible EZ-Astrophotography version: $version")
            }

            stackingFiles.clear()
            var stackingFilesLoading = true
            var stackingFilesIndex = 0
            while (stackingFilesLoading) {
                val filepath = properties.getProperty("stacking.$stackingFilesIndex.file")
                val x = properties.getProperty("stacking.$stackingFilesIndex.x")
                val y = properties.getProperty("stacking.$stackingFilesIndex.y")
                val base = properties.getProperty("stacking.$stackingFilesIndex.base")
                val stack = properties.getProperty("stacking.$stackingFilesIndex.stack")
                if (x != null && y != null) {
                    val image = loadImage(File(filepath))
                    stackingFiles.add(StackingFile(File(filepath), base.toBoolean(), x.toInt(), y.toInt(), stack.toBoolean(), 0.0, image))
                    if (base.toBoolean()) {
                        updateBaseImage(image)
                    }
                } else {
                    stackingFilesLoading = false
                }
                stackingFilesIndex++
            }
        }
    }

    private fun homeDirectory(): Path {
        return Paths.get(System.getProperty("user.home", "."))
    }

    companion object {
        private const val IMAGE_WIDTH = 600
        private const val IMAGE_HEIGHT = 600

        private const val ZOOM_WIDTH = 150
        private const val ZOOM_HEIGHT = 150

        private const val SPACING = 2.0

        private val INTEGER_FORMAT = DecimalFormat("##0")
        private val DOUBLE_FORMAT = DecimalFormat("##0.000")
        private val PERCENT_FORMAT = DecimalFormat("##0.000%")

        @JvmStatic
        fun main(args: Array<String>) {
            launch(ImageStackerApp::class.java)
        }
    }

    private class StackingFile(
            val file: File,
            base: Boolean = false,
            offsetX: Int = 0,
            offsetY: Int = 0,
            stack: Boolean = true,
            score: Double = 0.0,
            var image: DoubleImage? = null) {
        val xProperty = SimpleIntegerProperty(offsetX)
        val yProperty = SimpleIntegerProperty(offsetY)
        val baseProperty = SimpleBooleanProperty(base)
        val stackProperty = SimpleBooleanProperty(stack)
        val scoreProperty = SimpleDoubleProperty(score)
    }

    enum class StackingStrategy(val stacker: Stacker) {
        Average(AverageStacker()),
        Median(MedianStacker()),
        Max(MaxStacker())
    }
}