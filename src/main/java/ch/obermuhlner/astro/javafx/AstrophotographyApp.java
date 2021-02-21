package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.gradient.Point;
import ch.obermuhlner.astro.gradient.analysis.Histogram;
import ch.obermuhlner.astro.gradient.correction.SampleSubtraction;
import ch.obermuhlner.astro.gradient.correction.SimpleSampleSubtraction;
import ch.obermuhlner.astro.gradient.filter.GaussianBlurFilter;
import ch.obermuhlner.astro.gradient.filter.GradientInterpolationFilter;
import ch.obermuhlner.astro.gradient.operation.ImageOperation;
import ch.obermuhlner.astro.gradient.operation.SubtractLinearImageOperation;
import ch.obermuhlner.astro.image.ArrayDoubleImage;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageCreator;
import ch.obermuhlner.astro.image.ImageQuality;
import ch.obermuhlner.astro.image.ImageReader;
import ch.obermuhlner.astro.image.ImageWriter;
import ch.obermuhlner.astro.image.WriteThroughArrayDoubleImage;
import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.color.ColorUtil;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

public class AstrophotographyApp extends Application {

  private static final int IMAGE_WIDTH = 600;
  private static final int IMAGE_HEIGHT = 600;

  private static final int ZOOM_WIDTH = 150;
  private static final int ZOOM_HEIGHT = 150;

  private static final int HISTOGRAM_WIDTH = 150;
  private static final int HISTOGRAM_HEIGHT = 50;

  private static final int COLOR_CURVE_WIDTH = 150;
  private static final int COLOR_CURVE_HEIGHT = 150;

  private static final int COLOR_INDICATOR_SIZE = 15;

  private static final int SPACING = 2;

  private static final boolean ACCURATE_PREVIEW = true;

  private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("##0");
  private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("##0.000");
  private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("##0.000%");

  private static final String EZ_ASTRO_FILE_EXTENSION = ".ezastro";

  private final Path homeDirectory = homeDirectory();

  private final GradientInterpolationFilter gradientInterpolationFilter = new GradientInterpolationFilter();
  private ImageOperation gradientSubtractor = new SubtractLinearImageOperation();

  private final IntegerProperty zoomCenterXProperty = new SimpleIntegerProperty();
  private final IntegerProperty zoomCenterYProperty = new SimpleIntegerProperty();

  private final ObjectProperty<SampleChannel> zoomDeltaSampleChannelProperty = new SimpleObjectProperty<>(SampleChannel.Brightness);
  private final DoubleProperty zoomDeltaSampleFactorProperty = new SimpleDoubleProperty();

  private final ObjectProperty<GlowStrategy> glowStrategyProperty = new SimpleObjectProperty<>();

  private final IntegerProperty sampleRadiusProperty = new SimpleIntegerProperty();
  private final ObjectProperty<Color> samplePixelColorProperty = new SimpleObjectProperty<>();
  private final ObjectProperty<Color> sampleAverageColorProperty = new SimpleObjectProperty<>();
  private final ObjectProperty<Color> gradientPixelColorProperty = new SimpleObjectProperty<>();

  private final ObjectProperty<PointFinderStrategy> pointFinderStrategyProperty = new SimpleObjectProperty<>();
  private final DoubleProperty interpolationPowerProperty = new SimpleDoubleProperty();

  private final IntegerProperty blurRadiusProperty = new SimpleIntegerProperty();

  private final ObjectProperty<Color> singleGlowColorProperty = new SimpleObjectProperty<>(Color.BLACK);
  private Color medianAllColor;
  private Color averageAllColor;
  private Color darkestAllColor;

  private final DoubleProperty removalFactorProperty = new SimpleDoubleProperty();
  private final ObjectProperty<SubtractionStrategy> sampleSubtractionStrategyProperty = new SimpleObjectProperty<>();

  private final List<Color> crosshairColors = Arrays.asList(Color.YELLOW, Color.RED, Color.GREEN, Color.BLUE, Color.TRANSPARENT);
  private final ObjectProperty<Color> crosshairColorProperty = new SimpleObjectProperty<>(crosshairColors.get(0));
  private final ObjectProperty<Color> fixPointColorProperty = new SimpleObjectProperty<>(crosshairColors.get(1));

  private File inputFile;

  private Pane inputDecorationsPane;
  private Pane gradientDecorationsPane;
  private Pane outputDecorationsPane;
  private Pane deltaDecorationsPane;

  private WritableImage inputImage;
  private DoubleImage inputDoubleImage;
  private ImageView inputImageView;

  private WritableImage gradientImage;
  private DoubleImage gradientDoubleImage;
  private ImageView gradientImageView;

  private WritableImage outputImage;
  private DoubleImage outputDoubleImage;
  private ImageView outputImageView;

  private WritableImage deltaImage;
  private DoubleImage deltaDoubleImage;
  private ImageView deltaImageView;

  private WritableImage zoomInputImage;
  private DoubleImage zoomInputDoubleImage;
  private ImageView zoomInputImageView;

  private WritableImage zoomOutputImage;
  private DoubleImage zoomOutputDoubleImage;
  private ImageView zoomOutputImageView;

  private WritableImage zoomGradientImage;
  private DoubleImage zoomGradientDoubleImage;
  private ImageView zoomGradientImageView;

  private WritableImage zoomDeltaImage;
  private DoubleImage zoomDeltaDoubleImage;
  private ImageView zoomDeltaImageView;

  private final ObservableList<FixPoint> fixPoints = FXCollections.observableArrayList();

  private Spinner<Number> sampleRadiusSpinner;

  private Canvas colorCurveCanvas;

  private final Histogram inputHistogram = new Histogram(ColorModel.RGB, HISTOGRAM_WIDTH - 1);
  private Canvas inputHistogramCanvas;

  private final Histogram zoomInputHistogram = new Histogram(ColorModel.RGB, HISTOGRAM_WIDTH - 1);
  private Canvas zoomInputHistogramCanvas;

  private final Histogram zoomOutputHistogram = new Histogram(ColorModel.RGB, HISTOGRAM_WIDTH - 1);
  private Canvas zoomOutputHistogramCanvas;

  @Override
  public void start(Stage primaryStage) {
    Group root = new Group();
    Scene scene = new Scene(root);

    VBox vbox = new VBox(SPACING);
    root.getChildren().add(vbox);

    vbox.getChildren().add(createToolbar(primaryStage));

    HBox hbox = new HBox(SPACING);
    vbox.getChildren().add(hbox);

    {
      TabPane imageTabPane = new TabPane();
      hbox.getChildren().add(imageTabPane);
      imageTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

      Tab inputTab = new Tab("Input", createInputImageViewer());
      imageTabPane.getTabs().add(inputTab);
      inputTab.setTooltip(tooltip("Shows the input image with several overlays:\n"
          + "- the zoom window\n"
          + "- the fixpoints for gradient interpolation (initially empty)"));

      //imageTabPane.getTabs().add(new Tab("Details", new Label("TODO: Details")));

      Tab glowTab = new Tab("Glow", createGradientImageViewer());
      imageTabPane.getTabs().add(glowTab);
      glowTab.setTooltip(tooltip("Shows the calculated sky glow image that will be removed from the input image.\n"
          + "Switching away from the input tab will take a while to calculate the image."));

      Tab outputTab = new Tab("Output", createOutputImageViewer());
      imageTabPane.getTabs().add(outputTab);
      outputTab.setTooltip(tooltip("Shows the calculated output image with the sky glow removed from the input image.\n"
          + "Switching away from the input tab will take a while to calculate the image."));

      Tab deltaTab = new Tab("Delta", createDeltaImageViewer());
      imageTabPane.getTabs().add(deltaTab);
      outputTab.setTooltip(tooltip("Shows the difference between the glow image and input image.\n"
          + "The channel to calculate the difference can be selected: Red, Green, Blue, Hue, Saturation, Brightness\n"
          + "Blue colors indicate a positive, red a negative difference.\n"
          + "Switching away from the input tab will take a while to calculate the image."));

      imageTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if (oldValue == inputTab) {
          removeGradient(inputDoubleImage, gradientDoubleImage, outputDoubleImage);

          calculateDeltaImage(
              inputDoubleImage,
              gradientDoubleImage,
              deltaDoubleImage,
              zoomDeltaSampleChannelProperty.get().getColorModel(),
              zoomDeltaSampleChannelProperty.get().getSampleIndex(),
              zoomDeltaSampleFactorProperty.get());
        }
      });

    }

    {
      hbox.getChildren().add(createEditor());
    }


    primaryStage.setScene(scene);
    primaryStage.show();

    glowStrategyProperty.addListener((observable, oldValue, newValue) -> {
      updateZoom();
    });
    fixPoints.addListener(new ListChangeListener<FixPoint>() {
      @Override
      public void onChanged(Change<? extends FixPoint> c) {
        updateFixPoints();
      }
    });
    sampleRadiusProperty.addListener((observable, oldValue, newValue) -> {
      updateFixPoints();
    });
    pointFinderStrategyProperty.addListener((observable, oldValue, newValue) -> {
      gradientInterpolationFilter.setPointsFinder(pointFinderStrategyProperty.get().getPointsFinder());
      updateZoom();
    });
    interpolationPowerProperty.addListener((observable, oldValue, newValue) -> {
      gradientInterpolationFilter.setInterpolationPower(interpolationPowerProperty.get());
      updateZoom();
    });
    singleGlowColorProperty.addListener((observable, oldValue, newValue) -> {
      updateZoom();
    });
    blurRadiusProperty.addListener((observable, oldValue, newValue) -> {
      updateZoom();
    });
//    removalFactor.addListener((observable, oldValue, newValue) -> {
//      gradientInterpolationFilter.setRemovalFactor(removalFactor.get());
//      updateZoom();
//    });
    sampleSubtractionStrategyProperty.addListener((observable, oldValue, newValue) -> {
      gradientSubtractor = sampleSubtractionStrategyProperty.get().getOperation();
      updateZoom();
    });

    zoomCenterXProperty.addListener((observable, oldValue, newValue) -> {
      updateZoom();
    });
    zoomCenterYProperty.addListener((observable, oldValue, newValue) -> {
      updateZoom();
    });
    zoomDeltaSampleChannelProperty.addListener((observable, oldValue, newValue) -> {
      updateZoomDelta();
    });
    zoomDeltaSampleFactorProperty.addListener((observable, oldValue, newValue) -> {
      updateZoomDelta();
    });

    initializeValues();
  }

  private void removeGradient(DoubleImage input, DoubleImage gradient, DoubleImage output) {
    switch(glowStrategyProperty.get()) {
      case SingleColor:
        gradient.setPixels(ColorModel.RGB, toDoubleColorRGB(singleGlowColorProperty.get()));
        break;
      case Blur:
        GaussianBlurFilter gaussianBlurFilter = new GaussianBlurFilter(blurRadiusProperty.get(), ColorModel.RGB);
        gaussianBlurFilter.filter(input, gradient);
        break;
      case Gradient:
        gradientInterpolationFilter.filter(input, gradient);
        break;
    }
    gradientSubtractor.operation(input, gradient, output);
  }

  private void initializeValues() {
    pointFinderStrategyProperty.set(PointFinderStrategy.All);
    setSampleRadius(5);
    interpolationPowerProperty.set(3.0);
    blurRadiusProperty.set(100);
    removalFactorProperty.set(1.0);
    sampleSubtractionStrategyProperty.set(SubtractionStrategy.SubtractLinear);
  }

  void setSampleRadius(int value) {
    // workaround, because Spinner.valueProperty() is read only
    sampleRadiusSpinner.getValueFactory().setValue(value);
  }

  private void updateFixPoints() {
    inputDecorationsPane.getChildren().clear();
    for (FixPoint fixPoint : fixPoints) {
      Circle circle = new Circle(3);
      circle.setFill(Color.TRANSPARENT);
      circle.strokeProperty().bind(fixPointColorProperty);
      double x = fixPoint.x / inputImage.getWidth() * inputImageView.getBoundsInLocal().getWidth();
      double y = fixPoint.y / inputImage.getHeight() * inputImageView.getBoundsInLocal().getHeight();
      circle.setCenterX(x);
      circle.setCenterY(y);
      inputDecorationsPane.getChildren().add(circle);
    }

    gradientInterpolationFilter.setFixPoints(
        toPointList(fixPoints),
        inputDoubleImage,
        sampleRadiusProperty.get());

    updateZoom();
  }

  private Node createToolbar(Stage stage) {
    HBox box = new HBox(SPACING);

    Button openButton = new Button("Open ...");
    openButton.setTooltip(tooltip("Opens a new input image or EZ-Astro project."));
    Button saveButton = new Button("Save ...");
    saveButton.setTooltip(tooltip("Saves the calculated output image."));
    saveButton.setDisable(true);

    box.getChildren().add(openButton);
    openButton.setOnAction(event -> {
      openImageFile(stage);
      saveButton.setDisable(false);
    });

    box.getChildren().add(saveButton);
    saveButton.setOnAction(event -> {
      saveImageFile(stage);
    });

    Button crosshairColorButton = createColorButton(crosshairColorProperty, new Rectangle(10, 10));
    crosshairColorButton.setTooltip(tooltip("Toggles the color of the crosshair in the zoom images."));
    box.getChildren().add(crosshairColorButton);

    Button fixPointColorButton = createColorButton(fixPointColorProperty, new Circle(3));
    fixPointColorButton.setTooltip(tooltip("Toggles the color of the fix point markers in the input images."));
    box.getChildren().add(fixPointColorButton);

    return box;
  }

  private Button createColorButton(ObjectProperty<Color> colorProperty, Shape shape) {
    Button button = new Button();
    shape.setFill(Color.TRANSPARENT);
    shape.strokeProperty().bind(colorProperty);
    button.setGraphic(shape);

    button.setOnAction(event -> {
      int index = crosshairColors.indexOf(colorProperty.get());
      index = (index + 1) % crosshairColors.size();
      colorProperty.setValue(crosshairColors.get(index));
    });

    return button;
  }

  private void openImageFile(Stage stage) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(homeDirectory.toFile());
    fileChooser.setTitle("Open Input Image or EZ-Astrophotography Project");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Standard", "*" + EZ_ASTRO_FILE_EXTENSION, "*.tif", "*.tiff", "*.png", "*.jpg", "*.jpeg"));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Project", "*" + EZ_ASTRO_FILE_EXTENSION));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Image", "*.tif", "*.tiff", "*.png", "*.jpg", "*.jpeg"));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("All", "*"));
    final File chosenFile = fileChooser.showOpenDialog(stage);

    if (chosenFile != null) {
      ProgressDialog.show("Loading", "Loading input image ...", () -> {
        try {
          if (chosenFile.getPath().endsWith(EZ_ASTRO_FILE_EXTENSION)) {
            inputFile = loadProperties(chosenFile);
          } else {
            inputFile = chosenFile;
            loadImage(inputFile);
          }
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
  }

  private void saveImageFile(Stage stage) {
    File directory = null;
    if (inputFile != null) {
      directory = inputFile.getParentFile();
    }
    if (directory == null) {
      homeDirectory.toFile();
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(directory);
    fileChooser.setTitle("Save Image");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Standard", "*" + EZ_ASTRO_FILE_EXTENSION, "*.tif", "*.tiff", "*.png", "*.jpg", "*.jpeg"));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Project", "*" + EZ_ASTRO_FILE_EXTENSION));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Image", "*.tif", "*.tiff", "*.png", "*.jpg", "*.jpeg"));
    fileChooser.getExtensionFilters().add(new ExtensionFilter("All", "*"));
    File outputFile = fileChooser.showSaveDialog(stage);

    if (outputFile != null) {
      ProgressDialog.show("Saving", "Saving output image ...", () -> {
        try {
          saveImage(outputFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
  }

  private void saveImage(File outputFile) throws IOException {
    DoubleImage inputImage = inputDoubleImage;
    //DoubleImage inputImage = ImageReader.read(inputFile, ImageQuality.High);
    DoubleImage outputImage = createOutputImage(inputImage);

    removeGradient(inputImage, gradientDoubleImage, outputImage);

    ImageWriter.write(outputImage, outputFile);

    saveProperties(toPropertiesFile(outputFile));
  }

  private File toPropertiesFile(File outputFile) {
    return new File(outputFile.getPath() + EZ_ASTRO_FILE_EXTENSION);
  }

  private void saveProperties(File file) throws IOException {
    try(PrintWriter writer = new PrintWriter(new FileWriter(file))) {
      Properties properties = new Properties();

      properties.put("version", "0.0.1");
      properties.put("input", String.valueOf(inputFile));
      properties.put("glow.remover", "gradient");
      for (int i = 0; i < fixPoints.size(); i++) {
        properties.put("glow.remover.gradient.fixpoint." + i + ".x", String.valueOf(fixPoints.get(i).x));
        properties.put("glow.remover.gradient.fixpoint." + i + ".y", String.valueOf(fixPoints.get(i).y));
      }
      properties.put("glow.remover.gradient.sampleRadius", String.valueOf(sampleRadiusProperty.get()));
      properties.put("glow.remover.gradient.removalFactor", String.valueOf(removalFactorProperty.get()));
      properties.put("glow.remover.gradient.interpolationPower", String.valueOf(interpolationPowerProperty.get()));
      properties.put("glow.remover.sampleSubtractionStrategy", sampleSubtractionStrategyProperty.get().name());

      properties.store(writer, "EZ-Astrophotography\nhttps://github.com/eobermuhlner/ez-astrophotography");
    }
  }

  private File loadProperties(File file) throws IOException {
    File result;

    try (FileReader reader = new FileReader(file)) {
      Properties properties = new Properties();
      properties.load(reader);

      String version = properties.getProperty("version");
      if (!version.startsWith("0.")) {
        throw new IOException("Incompatible EZ-Astrophotography version: " + version);
      }

      result = new File(properties.getProperty("input"));
      loadImage(result);
      sampleSubtractionStrategyProperty.set(SubtractionStrategy.valueOf(properties.getProperty("glow.remover.sampleSubtractionStrategy")));

      if ("gradient".equals(properties.getProperty("glow.remover"))) {
        setSampleRadius(Integer.parseInt(properties.getProperty("glow.remover.gradient.sampleRadius")));
        removalFactorProperty.set(Double.parseDouble(properties.getProperty("glow.remover.gradient.removalFactor")));
        interpolationPowerProperty.set(Double.parseDouble(properties.getProperty("glow.remover.gradient.interpolationPower")));

        fixPoints.clear();
        boolean fixPointLoading = true;
        int fixPointIndex = 0;
        while (fixPointLoading) {
          String x = properties.getProperty("glow.remover.gradient.fixpoint." + fixPointIndex + ".x");
          String y = properties.getProperty("glow.remover.gradient.fixpoint." + fixPointIndex + ".y");
          if (x != null && y != null) {
            addFixPoint(Integer.parseInt(x), Integer.parseInt(y));
          } else {
            fixPointLoading = false;
          }
          fixPointIndex++;
        }
      }
    }

    return result;
  }

  private DoubleImage createOutputImage(DoubleImage inputImage) {
    int width = inputImage.getWidth();
    int height = inputImage.getHeight();
    return ImageCreator.create(width, height, ImageQuality.High);
  }

  private void loadImage(File file) throws IOException {
    inputDoubleImage = ImageReader.read(file, ImageQuality.High);

    int width = inputDoubleImage.getWidth();
    int height = inputDoubleImage.getHeight();

    inputImage = new WritableImage(width, height);
    double[] rgb = new double[3];
    PixelWriter pw = inputImage.getPixelWriter();
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        inputDoubleImage.getPixel(x, y, ColorModel.RGB, rgb);
        pw.setArgb(x, y, ColorUtil.toIntARGB(rgb));
      }
    }

    inputImageView.setImage(inputImage);

    gradientImage = new WritableImage(width, height);
    gradientDoubleImage = new WriteThroughArrayDoubleImage(new JavaFXWritableDoubleImage(gradientImage), ColorModel.RGB);
    gradientImageView.setImage(gradientImage);

    outputImage = new WritableImage(width, height);
    outputDoubleImage = new WriteThroughArrayDoubleImage(new JavaFXWritableDoubleImage(outputImage), ColorModel.RGB);
    outputImageView.setImage(outputImage);

    deltaImage = new WritableImage(width, height);
    deltaDoubleImage = new WriteThroughArrayDoubleImage(new JavaFXWritableDoubleImage(deltaImage), ColorModel.RGB);
    deltaImageView.setImage(deltaImage);

    medianAllColor = null;
    averageAllColor = null;
    darkestAllColor = null;
    // TODO consider background thread to fill the colors above
    fixPoints.clear();
    setZoom(width / 2, height / 2);

    updateInputHistogram();
  }

  private List<Point> toPointList(ObservableList<FixPoint> fixPoints) {
    List<Point> points = new ArrayList<>();
    for (FixPoint fixPoint : fixPoints) {
      points.add(new Point(fixPoint.x, fixPoint.y));
    }
    return points;
  }

  private Node createInputImageViewer() {
    VBox box = new VBox(SPACING);

    inputDecorationsPane = new Pane();
    inputDecorationsPane.setMouseTransparent(true);

    inputImageView = new ImageView();
    box.getChildren().add(withZoomRectangle(inputImageView, inputDecorationsPane));

    inputImageView.setPreserveRatio(true);
    inputImageView.setFitWidth(IMAGE_WIDTH);
    inputImageView.setFitHeight(IMAGE_HEIGHT);

    setupImageSelectionListener(inputImageView);

    return box;
  }

  private Node createGradientImageViewer() {
    VBox box = new VBox(SPACING);

    gradientDecorationsPane = new Pane();
    gradientDecorationsPane.setMouseTransparent(true);

    gradientImageView = new ImageView();
    box.getChildren().add(withZoomRectangle(gradientImageView, gradientDecorationsPane));

    gradientImageView.setPreserveRatio(true);
    gradientImageView.setFitWidth(IMAGE_WIDTH);
    gradientImageView.setFitHeight(IMAGE_HEIGHT);

    setupImageSelectionListener(gradientImageView);

    return box;
  }

  private Node createOutputImageViewer() {
    VBox box = new VBox(SPACING);

    outputDecorationsPane = new Pane();
    outputDecorationsPane.setMouseTransparent(true);

    outputImageView = new ImageView();
    box.getChildren().add(withZoomRectangle(outputImageView, outputDecorationsPane));

    outputImageView.setPreserveRatio(true);
    outputImageView.setFitWidth(IMAGE_WIDTH);
    outputImageView.setFitHeight(IMAGE_HEIGHT);

    setupImageSelectionListener(outputImageView);

    return box;
  }

  private Node createDeltaImageViewer() {
    VBox box = new VBox(SPACING);

    deltaDecorationsPane = new Pane();
    deltaDecorationsPane.setMouseTransparent(true);

    deltaImageView = new ImageView();
    box.getChildren().add(withZoomRectangle(deltaImageView, deltaDecorationsPane));

    deltaImageView.setPreserveRatio(true);
    deltaImageView.setFitWidth(IMAGE_WIDTH);
    deltaImageView.setFitHeight(IMAGE_HEIGHT);

    setupImageSelectionListener(deltaImageView);

    return box;
  }

  private void setupImageSelectionListener(ImageView imageView) {
    setMouseDragEvents(imageView, event -> {
      double imageViewWidth = imageView.getBoundsInLocal().getWidth();
      double imageViewHeight = imageView.getBoundsInLocal().getHeight();
      int zoomX = (int) (event.getX() * imageView.getImage().getWidth() / imageViewWidth);
      int zoomY = (int) (event.getY() * imageView.getImage().getHeight() / imageViewHeight);

      zoomX = Math.max(zoomX, 0);
      zoomY = Math.max(zoomY, 0);
      zoomX = Math.min(zoomX, (int) imageView.getImage().getWidth() - 1);
      zoomY = Math.min(zoomY, (int) imageView.getImage().getHeight() - 1);

      setZoom(zoomX, zoomY);
    });
  }

  private void setZoom(int x, int y) {
    zoomCenterXProperty.set(x);
    zoomCenterYProperty.set(y);

    updateZoom(x, y);
  }

  private void updateZoom() {
    updateZoom(zoomCenterXProperty.get(), zoomCenterYProperty.get());
  }

  private void updateZoom(int zoomX, int zoomY) {
    int zoomOffsetX = zoomX - ZOOM_WIDTH/2;
    int zoomOffsetY = zoomY - ZOOM_HEIGHT/2;

    if (inputDoubleImage == null) {
      return;
    }

    double[] rgb = new double[3];
    ColorUtil.toIntARGB(inputDoubleImage.getPixel(zoomX, zoomY, ColorModel.RGB, rgb));
    samplePixelColorProperty.set(new Color(rgb[ColorModel.RGB.R], rgb[ColorModel.RGB.G], rgb[ColorModel.RGB.B], 1.0));

    int sampleRadius1 = sampleRadiusProperty.get();
    inputDoubleImage.averagePixel(
        zoomX - sampleRadius1,
        zoomY - sampleRadius1,
        sampleRadius1 + sampleRadius1 + 1,
        sampleRadius1 + sampleRadius1 + 1,
        ColorModel.RGB,
        rgb
    );
    sampleAverageColorProperty.set(new Color(rgb[ColorModel.RGB.R], rgb[ColorModel.RGB.G], rgb[ColorModel.RGB.B], 1.0));

    zoomGradientDoubleImage.getPixel(ZOOM_WIDTH/2, ZOOM_HEIGHT/2, ColorModel.RGB, rgb);
    gradientPixelColorProperty.set(new Color(rgb[ColorModel.RGB.R], rgb[ColorModel.RGB.G], rgb[ColorModel.RGB.B], 1.0));

    zoomInputDoubleImage.setPixels(
        zoomOffsetX,
        zoomOffsetY,
        inputDoubleImage,
        0,
        0,
        ZOOM_WIDTH,
        ZOOM_HEIGHT,
        ColorModel.RGB,
        new double[] { 0, 0, 0 }
    );

    removeGradient(
        zoomInputDoubleImage,
        zoomGradientDoubleImage,
        zoomOutputDoubleImage);

    updateColorCurve();
    updateZoomHistogram();
    updateZoomDelta();
  }

  private void updateColorCurve() {
    ImageOperation subtractor = sampleSubtractionStrategyProperty.get().getOperation();

    drawColorCurve(colorCurveCanvas, subtractor, gradientPixelColorProperty.get());
  }

  private void drawColorCurve(Canvas canvas, ImageOperation subtractor, Color gradientColor) {
    final GraphicsContext gc = canvas.getGraphicsContext2D();

    final double canvasWidth = canvas.getWidth();
    final double canvasHeight = canvas.getHeight();

    final double inset = 2;
    final double chartWidth = canvasWidth - inset * 2;
    final double chartHeight = canvasHeight - inset * 2;

    final DoubleImage input = new ArrayDoubleImage(1, 1, ColorModel.RGB);
    final DoubleImage gradient = new ArrayDoubleImage(1, 1, ColorModel.RGB);
    final DoubleImage output = new ArrayDoubleImage(1, 1, ColorModel.RGB);
    final double[] color = new double[3];

    gc.setFill(Color.LIGHTGRAY);
    gc.fillRect(0, 0, canvasWidth, canvasHeight);

    gc.setLineWidth(2.0);

    double xStep = 1.0 / canvasWidth * 0.5;

    double lastCanvasX = 0.0;
    double lastCanvasYR = 0.0;
    double lastCanvasYG = 0.0;
    double lastCanvasYB = 0.0;
    for (double x = 0.0; x <= 1.0; x+=xStep) {
      color[ColorModel.RGB.R] = x;
      color[ColorModel.RGB.G] = x;
      color[ColorModel.RGB.B] = x;
      input.setPixel(0, 0, ColorModel.RGB, color);

      color[ColorModel.RGB.R] = gradientColor.getRed();
      color[ColorModel.RGB.G] = gradientColor.getGreen();
      color[ColorModel.RGB.B] = gradientColor.getBlue();
      gradient.setPixel(0, 0, ColorModel.RGB, color);

      subtractor.operation(input, gradient, output);
      output.getPixel(0, 0, ColorModel.RGB, color);
      double yR = color[ColorModel.RGB.R];
      double yG = color[ColorModel.RGB.G];
      double yB = color[ColorModel.RGB.B];

      double canvasX = x * chartWidth + inset;
      double canvasYR = canvasHeight - inset - yR * chartHeight;
      double canvasYG = canvasHeight - inset - yG * chartHeight;
      double canvasYB = canvasHeight - inset - yB * chartHeight;
      if (x != 0) {
        gc.setStroke(RED_SEMI);
        gc.strokeLine(lastCanvasX, lastCanvasYR, canvasX, canvasYR);
        gc.setStroke(GREEN_SEMI);
        gc.strokeLine(lastCanvasX, lastCanvasYG, canvasX, canvasYG);
        gc.setStroke(BLUE_SEMI);
        gc.strokeLine(lastCanvasX, lastCanvasYB, canvasX, canvasYB);
      }
      lastCanvasX = canvasX;
      lastCanvasYR = canvasYR;
      lastCanvasYG = canvasYG;
      lastCanvasYB = canvasYB;
    }
  }

  private void updateInputHistogram() {
    inputHistogram.sampleImage(inputDoubleImage);
    drawHistogram(inputHistogramCanvas, inputHistogram);
  }

  private void updateZoomHistogram() {
    zoomInputHistogram.sampleImage(zoomInputDoubleImage);
    drawHistogram(zoomInputHistogramCanvas, zoomInputHistogram);

    zoomOutputHistogram.sampleImage(zoomOutputDoubleImage);
    drawHistogram(zoomOutputHistogramCanvas, zoomOutputHistogram);
  }

  private static final Color RED_SEMI = new Color(1.0, 0.0, 0.0, 0.8);
  private static final Color GREEN_SEMI = new Color(0.0, 1.0, 0.0, 0.8);
  private static final Color BLUE_SEMI = new Color(0.0, 0.0, 1.0, 0.8);
  private void drawHistogram(Canvas histogramCanvas, Histogram histogram) {
    GraphicsContext gc = histogramCanvas.getGraphicsContext2D();

    double canvasWidth = histogramCanvas.getWidth();
    double canvasHeight = histogramCanvas.getHeight();

    gc.setFill(Color.LIGHTGRAY);
    gc.fillRect(0, 0, canvasWidth, canvasHeight);

    gc.setLineWidth(2.0);

    double prevR = histogram.getBin(ColorModel.RGB.R, 0) * canvasHeight;
    double prevG = histogram.getBin(ColorModel.RGB.G, 0) * canvasHeight;
    double prevB = histogram.getBin(ColorModel.RGB.B, 0) * canvasHeight;
    for (int binIndex = 1; binIndex < histogram.getBinCount(); binIndex++) {
      gc.setStroke(RED_SEMI);
      double r = histogram.getBin(ColorModel.RGB.R, binIndex) * canvasHeight;
      gc.strokeLine(binIndex - 1, canvasHeight - prevR, binIndex, canvasHeight - r);
      prevR = r;

      gc.setStroke(GREEN_SEMI);
      double g = histogram.getBin(ColorModel.RGB.G, binIndex) * canvasHeight;
      gc.strokeLine(binIndex - 1, canvasHeight - prevG, binIndex, canvasHeight - g);
      prevG = g;

      gc.setStroke(BLUE_SEMI);
      double b = histogram.getBin(ColorModel.RGB.B, binIndex) * canvasHeight;
      gc.strokeLine(binIndex - 1, canvasHeight - prevB, binIndex, canvasHeight - b);
      prevB = b;
    }
  }

  private void updateZoomDelta() {
    calculateDeltaImage(
        zoomInputDoubleImage,
        zoomGradientDoubleImage,
        zoomDeltaDoubleImage,
        zoomDeltaSampleChannelProperty.get().getColorModel(),
        zoomDeltaSampleChannelProperty.get().getSampleIndex(),
        zoomDeltaSampleFactorProperty.get());
  }

  private void calculateDeltaImage(DoubleImage image1, DoubleImage image2, DoubleImage deltaImage, ColorModel colorModel, int sampleIndex, double sampleFactor) {
    double[] sample1 = new double[3];
    double[] sample2 = new double[3];
    double[] output = new double[3];
    double[] rgb = new double[3];

    SampleSubtraction sampleSubtraction = new SimpleSampleSubtraction();

    for (int y = 0; y < image1.getHeight(); y++) {
      for (int x = 0; x < image1.getWidth(); x++) {
        image1.getPixel(x, y, ColorModel.RGB, sample1);
        image2.getPixel(x, y, ColorModel.RGB, sample2);
        sampleSubtraction.subtract(sample1, sample2, output);

        double delta = ColorUtil.sampleDistance(output, colorModel, sampleIndex, true);

        if (delta < 0) {
          rgb[ColorModel.RGB.R] = Math.min(1, -delta * sampleFactor);
          rgb[ColorModel.RGB.G] = Math.min(1, -delta * sampleFactor * 0.5);
          rgb[ColorModel.RGB.B] = Math.min(1, -delta * sampleFactor * 0.5);
        } else if (delta >= 0) {
          rgb[ColorModel.RGB.R] = Math.min(1, delta * sampleFactor * 0.5);
          rgb[ColorModel.RGB.G] = Math.min(1, delta * sampleFactor * 0.5);
          rgb[ColorModel.RGB.B] = Math.min(1, delta * sampleFactor);
        }

        deltaImage.setPixel(x, y, ColorModel.RGB, rgb);
      }
    }
  }

  private Node createEditor() {
    HBox mainBox = new HBox(SPACING);

    zoomInputImage = new WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT);
    if (ACCURATE_PREVIEW) {
      zoomInputDoubleImage = new WriteThroughArrayDoubleImage(new JavaFXWritableDoubleImage(zoomInputImage), ColorModel.RGB);
    } else {
      zoomInputDoubleImage = new JavaFXWritableDoubleImage(zoomInputImage);
    }
    zoomInputImageView = new ImageView(zoomInputImage);

    zoomOutputImage = new WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT);
    zoomOutputDoubleImage = new WriteThroughArrayDoubleImage(new JavaFXWritableDoubleImage(zoomOutputImage), ColorModel.RGB);
    zoomOutputImageView = new ImageView(zoomOutputImage);

    zoomGradientImage = new WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT);
    zoomGradientDoubleImage = new WriteThroughArrayDoubleImage(new JavaFXWritableDoubleImage(zoomGradientImage), ColorModel.RGB);
    zoomGradientImageView = new ImageView(zoomGradientImage);

    zoomDeltaImage = new WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT);
    zoomDeltaDoubleImage = new JavaFXWritableDoubleImage(zoomDeltaImage);
    zoomDeltaImageView = new ImageView(zoomDeltaImage);

    {
      GridPane mainGridPane = new GridPane();
      mainGridPane.setHgap(SPACING);
      mainGridPane.setVgap(SPACING);
      mainBox.getChildren().add(mainGridPane);

      int rowIndex = 0;

      {
        HBox sampleHBox = new HBox(SPACING);
        mainGridPane.add(sampleHBox, 0, rowIndex, 4, 1);

        sampleHBox.getChildren().add(new Label("X:"));
        TextField zoomCenterXTextField = new TextField();
        sampleHBox.getChildren().add(zoomCenterXTextField);
        zoomCenterXTextField.setPrefWidth(60);
        Bindings.bindBidirectional(zoomCenterXTextField.textProperty(), zoomCenterXProperty, INTEGER_FORMAT);

        sampleHBox.getChildren().add(new Label("Y:"));
        TextField zoomCenterYTextField = new TextField();
        sampleHBox.getChildren().add(zoomCenterYTextField);
        zoomCenterYTextField.setPrefWidth(60);
        Bindings.bindBidirectional(zoomCenterYTextField.textProperty(), zoomCenterYProperty, INTEGER_FORMAT);

        sampleHBox.getChildren().add(new Label("Radius:"));
        sampleRadiusSpinner = new Spinner<>(0, 30, 5);
        sampleHBox.getChildren().add(sampleRadiusSpinner);
        sampleRadiusSpinner.setTooltip(tooltip("Radius of the sample area used to calculate the color of gradient fix points.\n"
            + "The width and height of the sample area will be: 2*radius + 1"));
        sampleRadiusSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        sampleRadiusSpinner.setPrefWidth(70);
        sampleRadiusProperty.bind(sampleRadiusSpinner.valueProperty());

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

        rowIndex++;
      }

      {
        {
          mainGridPane.add(new Label("Zoom:"), 0, rowIndex);
          mainGridPane.add(new Label("Output Preview:"), 1, rowIndex);
          rowIndex++;
          mainGridPane.add(withCrosshair(zoomInputImageView), 0, rowIndex);
          mainGridPane.add(withCrosshair(zoomOutputImageView), 1, rowIndex);
          rowIndex++;
        }

        {
          mainGridPane.add(new Label("Glow:"), 0, rowIndex);

          HBox hbox = new HBox(SPACING);
          mainGridPane.add(hbox, 1, rowIndex);
          hbox.getChildren().add(new Label("Delta:"));

          ComboBox<SampleChannel> zoomDeltaColorModelComboBox = new ComboBox<>(FXCollections
              .observableArrayList(SampleChannel.values()));
          hbox.getChildren().add(zoomDeltaColorModelComboBox);
          tooltip(zoomDeltaColorModelComboBox, "Color channel used to show the delta between the input image and glow image.\n"
              + "The brightness delta is useful to determine how much color information is lost in the subtraction.");
          Bindings.bindBidirectional(zoomDeltaColorModelComboBox.valueProperty(), zoomDeltaSampleChannelProperty);

          Spinner<Number> zoomDeltaSampleFactorSpinner = new Spinner<>(1.0, 50.0, 20.0);
          hbox.getChildren().add(zoomDeltaSampleFactorSpinner);
          tooltip(zoomDeltaSampleFactorSpinner, "Factor used to exaggerate the delta value.");
          zoomDeltaSampleFactorSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
          zoomDeltaSampleFactorSpinner.setPrefWidth(70);
          zoomDeltaSampleFactorProperty.bind(zoomDeltaSampleFactorSpinner.valueProperty());
          rowIndex++;

          mainGridPane.add(withCrosshair(zoomGradientImageView), 0, rowIndex);
          mainGridPane.add(withCrosshair(zoomDeltaImageView), 1, rowIndex);
          tooltip(zoomDeltaImageView, "Shows the difference between the glow image and input image.\n"
              + "The channel to calculate the difference can be selected: Red, Green, Blue, Hue, Saturation, Brightness\n"
              + "Blue colors indicate a positive, red a negative difference."
              + "If the delta channel is set to the 'Brightness', red colors indicate that the output image brightness will be < 0 and therefore information is lost.");

          rowIndex++;
        }
      }

      {
        TabPane glowTabPane = new TabPane();
        mainGridPane.add(glowTabPane, 0, rowIndex, 4, 1);
        glowTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab glowSingleColorTab;
        Tab glowBlurTab;
        Tab glowGradientTab;

        {
          GridPane glowSingleColorGridPane = new GridPane();
          glowSingleColorGridPane.setHgap(SPACING);
          glowSingleColorGridPane.setVgap(SPACING);
          glowSingleColorTab = new Tab("Single Color", glowSingleColorGridPane);
          glowTabPane.getTabs().add(glowSingleColorTab);
          glowSingleColorTab.setTooltip(tooltip("Determine a single color that will be used uniformly to estimate the glow.\n"
              + "This is a good strategy if the glow is uniform over the entire image."));

          int glowSingleColorRowIndex = 0;

          {
            HBox buttonBox = new HBox(SPACING);
            glowSingleColorGridPane.add(buttonBox, 0, glowSingleColorRowIndex, 2, 1);

            {
              Button button = new Button("Median All");
              buttonBox.getChildren().add(button);
              tooltip(button, "Finds the median color of all pixels in the input image.");
              button.setOnAction(event -> {
                if (medianAllColor == null) {
                  medianAllColor = toJavafxColor(inputDoubleImage.medianPixel(ColorModel.RGB, null));
                }
                singleGlowColorProperty.set(medianAllColor);
              });
            }

            {
              Button button = new Button("Average All");
              buttonBox.getChildren().add(button);
              tooltip(button, "Finds the average color of all pixels in the input image.");
              button.setOnAction(event -> {
                if (averageAllColor == null) {
                  averageAllColor = toJavafxColor(inputDoubleImage.averagePixel(ColorModel.RGB, null));
                }
                singleGlowColorProperty.set(averageAllColor);
              });
            }

            {
              Button button = new Button("Darkest All");
              buttonBox.getChildren().add(button);
              tooltip(button, "Finds the darkest color of all pixels in the input image.");
              button.setOnAction(event -> {
                if (darkestAllColor == null) {
                  darkestAllColor = toJavafxColor(inputDoubleImage.darkestPixel(ColorModel.RGB, null));
                }
                singleGlowColorProperty.set(darkestAllColor);
              });
            }

            glowSingleColorRowIndex++;
          }

          {
            HBox buttonBox = new HBox(SPACING);
            glowSingleColorGridPane.add(buttonBox, 0, glowSingleColorRowIndex, 2, 1);

            {
              Button button = new Button("Median Zoom");
              buttonBox.getChildren().add(button);
              tooltip(button, "Finds the median color of the pixels in the zoom input image.");
              button.setOnAction(event -> {
                singleGlowColorProperty.set(toJavafxColor(zoomInputDoubleImage.medianPixel(ColorModel.RGB, null)));
              });
            }

            {
              Button button = new Button("Average Zoom");
              buttonBox.getChildren().add(button);
              tooltip(button, "Finds the average color of the pixels in the zoom input image.");
              button.setOnAction(event -> {
                singleGlowColorProperty.set(toJavafxColor(zoomInputDoubleImage.averagePixel(ColorModel.RGB, null)));
              });
            }

            {
              Button button = new Button("Darkest Zoom");
              buttonBox.getChildren().add(button);
              tooltip(button, "Finds the darkest color of the pixels in the zoom input image.");
              button.setOnAction(event -> {
                singleGlowColorProperty.set(toJavafxColor(zoomInputDoubleImage.darkestPixel(ColorModel.RGB, null)));
              });
            }

            glowSingleColorRowIndex++;
          }

          {
            HBox buttonBox = new HBox(SPACING);
            glowSingleColorGridPane.add(buttonBox, 0, glowSingleColorRowIndex, 2, 1);

            {
              Button button = new Button("Median Sample");
              buttonBox.getChildren().add(button);
              tooltip(button, "Finds the median color of the pixels in the sample radius of the zoom input image.");
              button.setOnAction(event -> {
                int x = ZOOM_WIDTH/2;
                int y = ZOOM_HEIGHT/2;
                int r = sampleRadiusProperty.get();
                singleGlowColorProperty.set(toJavafxColor(zoomInputDoubleImage.subImage(x-r, y-r, r+r+1, r+r+1).medianPixel(ColorModel.RGB, null)));
              });
            }

            {
              Button button = new Button("Average Sample");
              buttonBox.getChildren().add(button);
              tooltip(button, "Finds the average color of the pixels in the sample radius of the zoom input image.");
              button.setOnAction(event -> {
                int x = ZOOM_WIDTH/2;
                int y = ZOOM_HEIGHT/2;
                int r = sampleRadiusProperty.get();
                singleGlowColorProperty.set(toJavafxColor(zoomInputDoubleImage.subImage(x-r, y-r, r+r+1, r+r+1).averagePixel(ColorModel.RGB, null)));
              });
            }

            {
              Button button = new Button("Darkest Sample");
              buttonBox.getChildren().add(button);
              tooltip(button, "Finds the darkest color of the pixels in the sample radius of the zoom input image.");
              button.setOnAction(event -> {
                int x = ZOOM_WIDTH/2;
                int y = ZOOM_HEIGHT/2;
                int r = sampleRadiusProperty.get();
                singleGlowColorProperty.set(toJavafxColor(zoomInputDoubleImage.subImage(x-r, y-r, r+r+1, r+r+1).darkestPixel(ColorModel.RGB, null)));
              });
            }

            glowSingleColorRowIndex++;
          }

          {
            glowSingleColorGridPane.add(new Label("Glow Color:"), 0, glowSingleColorRowIndex);
            Rectangle sampleAverageRectangle = new Rectangle(COLOR_INDICATOR_SIZE, COLOR_INDICATOR_SIZE);
            glowSingleColorGridPane.add(sampleAverageRectangle, 1, glowSingleColorRowIndex);
            sampleAverageRectangle.fillProperty().bind(singleGlowColorProperty);
            glowSingleColorRowIndex++;
          }

          {
            glowSingleColorGridPane.add(new Label("Glow Red:"), 0, glowSingleColorRowIndex);
            Label glowRedLabel = new Label();
            glowSingleColorGridPane.add(glowRedLabel, 1, glowSingleColorRowIndex);
            singleGlowColorProperty.addListener((observable, oldValue, newValue) -> {
              glowRedLabel.setText(PERCENT_FORMAT.format(newValue.getRed()));
            });
            glowSingleColorRowIndex++;
          }

          {
            glowSingleColorGridPane.add(new Label("Glow Green:"), 0, glowSingleColorRowIndex);
            Label glowGreenLabel = new Label();
            glowSingleColorGridPane.add(glowGreenLabel, 1, glowSingleColorRowIndex);
            singleGlowColorProperty.addListener((observable, oldValue, newValue) -> {
              glowGreenLabel.setText(PERCENT_FORMAT.format(newValue.getGreen()));
            });
            glowSingleColorRowIndex++;
          }

          {
            glowSingleColorGridPane.add(new Label("Glow Blue:"), 0, glowSingleColorRowIndex);
            Label glowBlueLabel = new Label();
            glowSingleColorGridPane.add(glowBlueLabel, 1, glowSingleColorRowIndex);
            singleGlowColorProperty.addListener((observable, oldValue, newValue) -> {
              glowBlueLabel.setText(PERCENT_FORMAT.format(newValue.getBlue()));
            });
            glowSingleColorRowIndex++;
          }
        }

        {
          GridPane glowBlurGridPane = new GridPane();
          glowBlurGridPane.setHgap(SPACING);
          glowBlurGridPane.setVgap(SPACING);
          glowBlurTab = new Tab("Blur", glowBlurGridPane);
          glowTabPane.getTabs().add(glowBlurTab);
          glowBlurTab.setTooltip(tooltip("Blurs the input image to calculate the glow image.\n"
              + "This is a good strategy for images where the object of interest occupies only a small area."));

          int glowBlurRowIndex = 0;

          {
            glowBlurGridPane.add(new Label("Blur Radius:"), 0, glowBlurRowIndex);
            TextField blurRadiusTextField = new TextField();
            glowBlurGridPane.add(blurRadiusTextField, 1, glowBlurRowIndex);
            Bindings.bindBidirectional(blurRadiusTextField.textProperty(), blurRadiusProperty, INTEGER_FORMAT);
            glowBlurRowIndex++;
          }
        }

        {
          GridPane glowInterpolateGridPane = new GridPane();
          glowInterpolateGridPane.setHgap(SPACING);
          glowInterpolateGridPane.setVgap(SPACING);
          glowGradientTab = new Tab("Gradient", glowInterpolateGridPane);
          glowTabPane.getTabs().add(glowGradientTab);
          glowGradientTab.setTooltip(tooltip("Interpolates the glow between two or more points.\n"
              + "This is a good strategy for images with a glow that shows a strong gradient."));

          int glowInterpolateRowIndex = 0;

          {
            HBox fixPointToolbar = new HBox(SPACING);
            glowInterpolateGridPane.add(fixPointToolbar, 0, glowInterpolateRowIndex, 4, 1);

            Button addFixPointButton = new Button("Add");
            fixPointToolbar.getChildren().add(addFixPointButton);
            tooltip(addFixPointButton, "Adds a new fix point to interpolate the glow.\n"
                + "Make sure to set points in areas that only contain background glow and no nebula or stars.\n"
                + "It is best to define an odd number of points - three points is usually a good number.");
            addFixPointButton.setOnAction(event -> {
              int x = zoomCenterXProperty.get();
              int y = zoomCenterYProperty.get();
              addFixPoint(x, y);
            });

            Button clearFixPointButton = new Button("Clear");
            fixPointToolbar.getChildren().add(clearFixPointButton);
            clearFixPointButton.setOnAction(event -> {
              fixPoints.clear();
            });

            glowInterpolateRowIndex++;
          }

          {
            TableView<FixPoint> fixPointTableView = new TableView<>(fixPoints);
            glowInterpolateGridPane.add(fixPointTableView, 0, glowInterpolateRowIndex, 4, 1);
            fixPointTableView.setPlaceholder(new Label("Add points to define the background gradient."));
            fixPointTableView.setPrefHeight(100);
            fixPointTableView.setRowFactory(new Callback<TableView<FixPoint>, TableRow<FixPoint>>() {
              @Override
              public TableRow<FixPoint> call(TableView<FixPoint> param) {
                TableRow<FixPoint> tableRow = new TableRow<>();
                MenuItem gotoMenuItem = new MenuItem("Go To");
                gotoMenuItem.setOnAction(event -> {
                  setZoom(tableRow.getItem().x, tableRow.getItem().y);
                });
                MenuItem removeMenuItem = new MenuItem("Remove");
                removeMenuItem.setOnAction(event -> {
                  fixPoints.remove(tableRow.getItem());
                });
                tableRow.setContextMenu(new ContextMenu(
                    gotoMenuItem,
                    removeMenuItem
                ));
                return tableRow;
              }
            });
            addTableColumn(fixPointTableView, "X", 40, fixPoint -> {
              return new ReadOnlyIntegerWrapper(fixPoint.x);
            });
            addTableColumn(fixPointTableView, "Y", 40, fixPoint -> {
              return new ReadOnlyIntegerWrapper(fixPoint.y);
            });
            addTableColumn(fixPointTableView, "Color", 50, fixPoint -> {
              Rectangle rectangle = new Rectangle(COLOR_INDICATOR_SIZE, COLOR_INDICATOR_SIZE);
              rectangle.setFill(fixPoint.color);
              return new ReadOnlyObjectWrapper<>(rectangle);
            });
            addTableColumn(fixPointTableView, "Red", 70, fixPoint -> {
              return new ReadOnlyStringWrapper(PERCENT_FORMAT.format(fixPoint.color.getRed()));
            });
            addTableColumn(fixPointTableView, "Green", 70, fixPoint -> {
              return new ReadOnlyStringWrapper(PERCENT_FORMAT.format(fixPoint.color.getGreen()));
            });
            addTableColumn(fixPointTableView, "Blue", 70, fixPoint -> {
              return new ReadOnlyStringWrapper(PERCENT_FORMAT.format(fixPoint.color.getBlue()));
            });
            glowInterpolateRowIndex++;
          }

          {
            {
              glowInterpolateGridPane.add(new Label("Point Finder:"), 0, glowInterpolateRowIndex);
              ComboBox<PointFinderStrategy> pointFinderComboBox = new ComboBox<>(FXCollections
                  .observableArrayList(PointFinderStrategy.values()));
              glowInterpolateGridPane.add(pointFinderComboBox, 1, glowInterpolateRowIndex);
              Bindings.bindBidirectional(pointFinderComboBox.valueProperty(), pointFinderStrategyProperty);
              glowInterpolateRowIndex++;
            }

            {
              glowInterpolateGridPane.add(new Label("Interpolation Power:"), 0, glowInterpolateRowIndex);
              TextField interpolationPowerTextField = new TextField();
              glowInterpolateGridPane.add(interpolationPowerTextField, 1, glowInterpolateRowIndex);
              Bindings.bindBidirectional(interpolationPowerTextField.textProperty(),
                  interpolationPowerProperty, DOUBLE_FORMAT);
              glowInterpolateRowIndex++;
            }
          }
        }

        glowTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
          if (newValue == glowSingleColorTab) {
            glowStrategyProperty.set(GlowStrategy.SingleColor);
          } else if (newValue == glowBlurTab) {
            glowStrategyProperty.set(GlowStrategy.Blur);
          } else if (newValue == glowGradientTab) {
            glowStrategyProperty.set(GlowStrategy.Gradient);
          }
        });

        glowTabPane.getSelectionModel().select(glowGradientTab);
      }

      {
        GridPane subtractionGridPane = new GridPane();
        subtractionGridPane.setHgap(SPACING);
        subtractionGridPane.setVgap(SPACING);
        mainBox.getChildren().add(subtractionGridPane);

        int algorithmRowIndex = 0;

        {
          subtractionGridPane.add(new Label("Removal:"), 0, algorithmRowIndex);
          TextField removalFactorTextField = new TextField();
          subtractionGridPane.add(removalFactorTextField, 1, algorithmRowIndex);
          Bindings.bindBidirectional(removalFactorTextField.textProperty(), removalFactorProperty, PERCENT_FORMAT);
          algorithmRowIndex++;
        }

        {
          subtractionGridPane.add(new Label("Sample Subtraction:"), 0, algorithmRowIndex);
          ComboBox<SubtractionStrategy> sampleSubtractionComboBox = new ComboBox<>(FXCollections
              .observableArrayList(SubtractionStrategy.values()));
          subtractionGridPane.add(sampleSubtractionComboBox, 1, algorithmRowIndex);
          tooltip(sampleSubtractionComboBox, "Different strategies to subtract the calculated glow from the input image.\n"
              + "Subtract: Simply subtracts the RGB values of the glow.\n"
              + "Subtract Linear: Subtracts the RGB values of the glow and corrects the remaining value linearly.\n"
              + "Spline 1%: Uses a spline function to reduce the RGB value of the glow to 1%.\n"
              + "Spline 1% + Stretch: Uses a spline function to reduce the RGB value of the glow to 1% - stretching the remaining value non-linearly.\n"
              + "Spline 10%: Uses a spline function to reduce the RGB value of the glow to 10%.\n");
          Bindings.bindBidirectional(sampleSubtractionComboBox.valueProperty(), sampleSubtractionStrategyProperty);
          algorithmRowIndex++;
        }

        {
          subtractionGridPane.add(new Label("Curve:"), 0, algorithmRowIndex);
          colorCurveCanvas = new Canvas(COLOR_CURVE_WIDTH, COLOR_CURVE_HEIGHT);
          subtractionGridPane.add(colorCurveCanvas, 1, algorithmRowIndex);
          tooltip(colorCurveCanvas, "Color curve shows how the RGB values for the current pixel from the input image (x-axis) to the output image (y-axis) are transformed.");
          algorithmRowIndex++;

          subtractionGridPane.add(new Label("Input:"), 0, algorithmRowIndex);
          inputHistogramCanvas = new Canvas(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT);
          subtractionGridPane.add(inputHistogramCanvas, 1, algorithmRowIndex);
          tooltip(inputHistogramCanvas, "Histogram of the RGB values over the entire input image.");
          algorithmRowIndex++;

          subtractionGridPane.add(new Label("Zoom Input:"), 0, algorithmRowIndex);
          zoomInputHistogramCanvas = new Canvas(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT);
          subtractionGridPane.add(zoomInputHistogramCanvas, 1, algorithmRowIndex);
          tooltip(zoomInputHistogramCanvas, "Histogram of the RGB values over the zoom input image.");
          algorithmRowIndex++;

          subtractionGridPane.add(new Label("Zoom Output:"), 0, algorithmRowIndex);
          zoomOutputHistogramCanvas = new Canvas(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT);
          subtractionGridPane.add(zoomOutputHistogramCanvas, 1, algorithmRowIndex);
          tooltip(zoomOutputHistogramCanvas, "Histogram of the RGB values over the zoom output image.");
          algorithmRowIndex++;
        }
      }
    }

    setupZoomDragEvents(zoomInputImageView);
    setupZoomDragEvents(zoomOutputImageView);
    setupZoomDragEvents(zoomGradientImageView);
    setupZoomDragEvents(zoomDeltaImageView);

    return mainBox;
  }

  private Color toJavafxColor(double[] color) {
    return new Color(
        color[ColorModel.RGB.R],
        color[ColorModel.RGB.G],
        color[ColorModel.RGB.B],
        1.0);
  }

  private double[] toDoubleColorRGB(Color color) {
    return new double[] { color.getRed(), color.getGreen(), color.getBlue() };
  }

  private void addFixPoint(int x, int y) {
    int sampleRadius = sampleRadiusProperty.get();
    double[] color = inputDoubleImage.averagePixel(
        x - sampleRadius,
        y - sampleRadius,
        sampleRadius + sampleRadius + 1,
        sampleRadius + sampleRadius + 1,
        ColorModel.RGB,
        null
    );
    fixPoints.add(new FixPoint(x, y, new Color(color[0], color[1], color[2], 1.0)));
  }

  private Node withZoomRectangle(ImageView imageView, Pane zoomRectanglePane) {
    Rectangle rectangle = new Rectangle();
    rectangle.setMouseTransparent(true);
    rectangle.strokeProperty().bind(crosshairColorProperty);
    rectangle.setFill(Color.TRANSPARENT);

    zoomCenterXProperty.addListener((observable, oldValue, newValue) -> {
      updateZoomRectangle(rectangle);
    });
    zoomCenterYProperty.addListener((observable, oldValue, newValue) -> {
      updateZoomRectangle(rectangle);
    });
    inputImageView.imageProperty().addListener((observable, oldValue, newValue) -> {
      updateZoomRectangle(rectangle);
    });

    return new Pane(imageView, rectangle, zoomRectanglePane);
  }

  private void updateZoomRectangle(Rectangle rectangle) {
    double width = ZOOM_WIDTH / inputImage.getWidth() * inputImageView.getBoundsInLocal().getWidth();
    double height = ZOOM_HEIGHT / inputImage.getHeight() * inputImageView.getBoundsInLocal().getHeight();
    double x = zoomCenterXProperty.get() / inputImage.getWidth() * inputImageView.getBoundsInLocal().getWidth();
    double y = zoomCenterYProperty.get() / inputImage.getHeight() * inputImageView.getBoundsInLocal().getHeight();
    rectangle.setX(x - width / 2);
    rectangle.setY(y - height / 2);
    rectangle.setWidth(width);
    rectangle.setHeight(height);
  }

  private Node withCrosshair(ImageView imageView) {
    IntegerBinding size = sampleRadiusProperty.multiply(2).add(1);

    Rectangle rectangle = new Rectangle();
    rectangle.setMouseTransparent(true);
    rectangle.widthProperty().bind(size);
    rectangle.heightProperty().bind(size);
    rectangle.strokeProperty().bind(crosshairColorProperty);
    rectangle.setFill(Color.TRANSPARENT);
    rectangle.setX(ZOOM_WIDTH/2);
    rectangle.setY(ZOOM_HEIGHT/2);

    return new StackPane(imageView, rectangle);
  }

  private <E, V> TableColumn<E, V> addTableColumn(TableView<E> tableView, String header, double prefWidth, Function<E, ObservableValue<V>> valueFunction) {
    TableColumn<E, V> column = new TableColumn<>(header);
    column.setPrefWidth(prefWidth);
    column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<E,V>, ObservableValue<V>>() {
      @Override
      public ObservableValue<V> call(CellDataFeatures<E, V> cellData) {
        return valueFunction.apply(cellData.getValue());
      }
    });
    tableView.getColumns().add(column);
    return column;
  }

  private <E, V> TableColumn<E, V> addTableColumn(TableView<E> tableView, String header, double prefWidth, Function<E, ObservableValue<V>> valueFunction, Function<V, Node> nodeFunction) {
    TableColumn<E, V> column = new TableColumn<>(header);
    column.setPrefWidth(prefWidth);
    column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<E,V>, ObservableValue<V>>() {
      @Override
      public ObservableValue<V> call(CellDataFeatures<E, V> cellData) {
        return valueFunction.apply(cellData.getValue());
      }
    });
    column.setCellFactory(new Callback<TableColumn<E, V>, TableCell<E, V>>() {
      @Override
      public TableCell<E, V> call(TableColumn<E, V> cellColumn) {
        return new TableCell<E, V>() {
          @Override
          protected void updateItem(V item, boolean empty) {
            if (empty) {
              setGraphic(null);
            } else {
              setGraphic(nodeFunction.apply(item));
            }
            super.updateItem(item, empty);
          }
        };
      }
    });
    tableView.getColumns().add(column);
    return column;
  }

  Double zoomDragX = null;
  Double zoomDragY = null;
  private void setupZoomDragEvents(ImageView imageView) {
    imageView.setOnMousePressed(event -> {
      zoomDragX = event.getX();
      zoomDragY = event.getY();
    });

    imageView.setOnMouseDragged(event -> {
      double deltaX = zoomDragX - event.getX();
      double deltaY = zoomDragY - event.getY();
      zoomDragX = event.getX();
      zoomDragY = event.getY();

      int zoomX = zoomCenterXProperty.get() + (int) (deltaX);
      int zoomY = zoomCenterYProperty.get() + (int) (deltaY);

      zoomX = Math.max(zoomX, 0);
      zoomY = Math.max(zoomY, 0);
      zoomX = Math.min(zoomX, (int) inputImage.getWidth() - 1);
      zoomY = Math.min(zoomY, (int) inputImage.getHeight() - 1);

      zoomCenterXProperty.set(zoomX);
      zoomCenterYProperty.set(zoomY);

      updateZoom(zoomX, zoomY);
    });

    imageView.setOnMouseDragReleased(event -> {
      zoomDragX = null;
      zoomDragY = null;
    });
  }

  private void setMouseDragEvents(Node node, EventHandler<? super MouseEvent> handler) {
    node.setOnMouseClicked(handler);
    node.setOnMouseDragged(handler);
    node.setOnMouseReleased(handler);
  }

  private void tooltip(Node node, String text) {
    Tooltip.install(node, tooltip(text));
  }

  private Tooltip tooltip(String text) {
    return new Tooltip(text);
  }

  private Path homeDirectory() {
    Path path = Paths.get(System.getProperty("user.home", "."));
    //path.toFile().mkdirs();
    return path;
  }

  public static void main(String[] args) {
    launch();
  }

  private static class FixPoint {
    public final int x;
    public final int y;
    public final Color color;

    public FixPoint(int x, int y, Color color) {
      this.x = x;
      this.y = y;
      this.color = color;
    }
  }
}