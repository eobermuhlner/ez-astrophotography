package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.gradient.GradientRemover;
import ch.obermuhlner.astro.gradient.Point;
import ch.obermuhlner.astro.image.ColorModel;
import ch.obermuhlner.astro.image.ColorUtil;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageCreator;
import ch.obermuhlner.astro.image.ImageQuality;
import ch.obermuhlner.astro.image.ImageReader;
import ch.obermuhlner.astro.image.ImageUtil;
import ch.obermuhlner.astro.image.ImageWriter;
import ch.obermuhlner.astro.image.WriteThroughArrayDoubleImage;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class AstrophotographyApp extends Application {

  private static final int IMAGE_WIDTH = 600;
  private static final int IMAGE_HEIGHT = 600;

  private static final int ZOOM_WIDTH = 150;
  private static final int ZOOM_HEIGHT = 150;

  private static final int SPACING = 2;

  private static final boolean ACCURATE_PREVIEW = true;

  private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("##0");
  private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("##0.000");
  private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("##0.000%");

  private final Path homeDirectory = homeDirectory();

  private final GradientRemover gradientRemover = new GradientRemover();

  private final IntegerProperty zoomCenterX = new SimpleIntegerProperty();
  private final IntegerProperty zoomCenterY = new SimpleIntegerProperty();

  private final ObjectProperty<SampleChannel> zoomDeltaSampleChannel = new SimpleObjectProperty<>(SampleChannel.Brightness);

  private final IntegerProperty sampleRadius = new SimpleIntegerProperty();

  private final ObjectProperty<PointFinderStrategy> pointFinderStrategy = new SimpleObjectProperty<>();
  private final DoubleProperty interpolationPower = new SimpleDoubleProperty();
  private final DoubleProperty removalFactor = new SimpleDoubleProperty();

  private final List<Color> crosshairColors = Arrays.asList(Color.YELLOW, Color.RED, Color.GREEN, Color.BLUE, Color.TRANSPARENT);
  private final ObjectProperty<Color> crosshairColor = new SimpleObjectProperty<>(crosshairColors.get(0));
  private final ObjectProperty<Color> fixPointColor = new SimpleObjectProperty<>(crosshairColors.get(1));

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

  private WritableImage zoomPreviewImage;
  private DoubleImage zoomPreviewDoubleImage;
  private ImageView zoomPreviewImageView;

  private WritableImage zoomGradientImage;
  private DoubleImage zoomGradientDoubleImage;
  private ImageView zoomGradientImageView;

  private WritableImage zoomDeltaImage;
  private DoubleImage zoomDeltaDoubleImage;
  private ImageView zoomDeltaImageView;

  private final ObservableList<FixPoint> fixPoints = FXCollections.observableArrayList();

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

      //imageTabPane.getTabs().add(new Tab("Details", new Label("TODO: Details")));

      Tab gradientTab = new Tab("Gradient", createGradientImageViewer());
      imageTabPane.getTabs().add(gradientTab);
      Tab outputTab = new Tab("Output", createOutputImageViewer());
      imageTabPane.getTabs().add(outputTab);
      Tab deltaTab = new Tab("Delta", createDeltaImageViewer());
      imageTabPane.getTabs().add(deltaTab);

      imageTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if (oldValue == inputTab) {
          gradientRemover.removeGradient(inputDoubleImage, gradientDoubleImage, outputDoubleImage);

          calculateDiffImage(
              inputDoubleImage,
              gradientDoubleImage,
              deltaDoubleImage,
              zoomDeltaSampleChannel.get().getColorModel(),
              zoomDeltaSampleChannel.get().getSampleIndex());
        }
      });

    }

    {
      TabPane editorTabPane = new TabPane();
      hbox.getChildren().add(editorTabPane);
      editorTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

      Tab editorTab = new Tab("Editor", createEditor());
      editorTabPane.getTabs().add(editorTab);
    }


    primaryStage.setScene(scene);
    primaryStage.show();

    fixPoints.addListener(new ListChangeListener<FixPoint>() {
      @Override
      public void onChanged(Change<? extends FixPoint> c) {
        updateFixPoints();
      }
    });
    sampleRadius.addListener((observable, oldValue, newValue) -> {
      updateFixPoints();
    });
    pointFinderStrategy.addListener((observable, oldValue, newValue) -> {
      gradientRemover.setPointsFinder(pointFinderStrategy.get().getPointsFinder());
      updateZoom();
    });
    interpolationPower.addListener((observable, oldValue, newValue) -> {
      gradientRemover.setInterpolationPower(interpolationPower.get());
      updateZoom();
    });
    removalFactor.addListener((observable, oldValue, newValue) -> {
      gradientRemover.setRemovalFactor(removalFactor.get());
      updateZoom();
    });

    zoomCenterX.addListener((observable, oldValue, newValue) -> {
      updateZoom();
    });
    zoomCenterY.addListener((observable, oldValue, newValue) -> {
      updateZoom();
    });
    zoomDeltaSampleChannel.addListener((observable, oldValue, newValue) -> {
      updateZoom();
    });

    initializeValues();
  }

  private void initializeValues() {
    pointFinderStrategy.set(PointFinderStrategy.All);
    interpolationPower.set(3.0);
    removalFactor.set(1.0);
  }

  private void updateFixPoints() {
    inputDecorationsPane.getChildren().clear();
    for (FixPoint fixPoint : fixPoints) {
      Circle circle = new Circle(3);
      circle.setFill(Color.TRANSPARENT);
      circle.strokeProperty().bind(fixPointColor);
      double x = fixPoint.x / inputImage.getWidth() * inputImageView.getBoundsInLocal().getWidth();
      double y = fixPoint.y / inputImage.getHeight() * inputImageView.getBoundsInLocal().getHeight();
      circle.setCenterX(x);
      circle.setCenterY(y);
      inputDecorationsPane.getChildren().add(circle);
    }

    gradientRemover.setFixPoints(
        toPointList(fixPoints),
        inputDoubleImage,
        sampleRadius.get());

    updateZoom();
  }

  private Node createToolbar(Stage stage) {
    HBox box = new HBox(SPACING);

    Button openButton = new Button("Open ...");
    Button saveButton = new Button("Save ...");
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

    Button crosshairColorButton = createColorButton(crosshairColor, new Rectangle(10, 10));
    crosshairColorButton.setTooltip(new Tooltip("Toggles the color of the crosshair in the zoom images."));
    box.getChildren().add(crosshairColorButton);

    Button fixPointColorButton = createColorButton(fixPointColor, new Circle(3));
    fixPointColorButton.setTooltip(new Tooltip("Toggles the color of the fix point markers in the input images."));
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
    fileChooser.setTitle("Open Image");
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Images", "*.tif", "*.tiff", "*.png", "*.jpg", "*.jpeg"));
    inputFile = fileChooser.showOpenDialog(stage);

    if (inputFile != null) {
      ProgressDialog.show("Loading", "Loading input image ...", () -> {
        try {
          loadImage(inputFile);
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
    fileChooser.getExtensionFilters().add(new ExtensionFilter("Images", "*.tif", "*.tiff", "*.png", "*.jpg", "*.jpeg"));
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

    gradientRemover.removeGradient(inputImage, null, outputImage);

    ImageWriter.write(outputImage, outputFile);
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
        pw.setArgb(x, y, 0xff000000 | ColorUtil.toIntRGB(rgb));
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

    fixPoints.clear();
    setZoom(width / 2, height / 2);
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
    zoomCenterX.set(x);
    zoomCenterY.set(y);

    updateZoom(x, y);
  }

  private void updateZoom() {
    updateZoom(zoomCenterX.get(), zoomCenterY.get());
  }

  private void updateZoom(int zoomX, int zoomY) {
    int zoomOffsetX = zoomX - ZOOM_WIDTH/2;
    int zoomOffsetY = zoomY - ZOOM_HEIGHT/2;

    ImageUtil.copyPixels(
        inputDoubleImage,
        zoomOffsetX,
        zoomOffsetY,
        zoomInputDoubleImage,
        0,
        0,
        ZOOM_WIDTH,
        ZOOM_HEIGHT,
        ColorModel.RGB);

    gradientRemover.removeGradient(
        zoomInputDoubleImage,
        zoomGradientDoubleImage,
        zoomPreviewDoubleImage,
        zoomOffsetX,
        zoomOffsetY);

    calculateDiffImage(
        zoomInputDoubleImage,
        zoomGradientDoubleImage,
        zoomDeltaDoubleImage,
        zoomDeltaSampleChannel.get().getColorModel(),
        zoomDeltaSampleChannel.get().getSampleIndex());
  }

  private void calculateDiffImage(DoubleImage image1, DoubleImage image2, DoubleImage deltaImage, ColorModel colorModel, int sampleIndex) {
    double[] sample1 = new double[3];
    double[] sample2 = new double[3];
    double[] rgb = new double[3];

    for (int y = 0; y < image1.getHeight(); y++) {
      for (int x = 0; x < image1.getWidth(); x++) {
        image1.getPixel(x, y, colorModel, sample1);
        image2.getPixel(x, y, colorModel, sample2);
        double delta = ColorUtil.sampleDistance(sample1, sample2, colorModel, sampleIndex, true);

        if (delta < 0) {
          rgb[ColorModel.R] = Math.min(1, -delta * 20);
          rgb[ColorModel.G] = Math.min(1, -delta * 10);
          rgb[ColorModel.B] = Math.min(1, -delta * 10);
        } else if (delta >= 0) {
          rgb[ColorModel.R] = Math.min(1, delta * 10);
          rgb[ColorModel.G] = Math.min(1, delta * 10);
          rgb[ColorModel.B] = Math.min(1, delta * 20);
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

    zoomPreviewImage = new WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT);
    zoomPreviewDoubleImage = new JavaFXWritableDoubleImage(zoomPreviewImage);
    zoomPreviewImageView = new ImageView(zoomPreviewImage);

    zoomGradientImage = new WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT);
    zoomGradientDoubleImage = new WriteThroughArrayDoubleImage(new JavaFXWritableDoubleImage(zoomGradientImage), ColorModel.RGB);
    zoomGradientImageView = new ImageView(zoomGradientImage);

    zoomDeltaImage = new WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT);
    zoomDeltaDoubleImage = new JavaFXWritableDoubleImage(zoomDeltaImage);
    zoomDeltaImageView = new ImageView(zoomDeltaImage);

    {
      GridPane mainGridPane = new GridPane();
      mainBox.getChildren().add(mainGridPane);
      mainGridPane.setHgap(4);
      mainGridPane.setVgap(4);

      int rowIndex = 0;

      {
        HBox sampleHBox = new HBox(4);
        mainGridPane.add(sampleHBox, 0, rowIndex, 4, 1);

        sampleHBox.getChildren().add(new Label("X:"));
        TextField zoomCenterXTextField = new TextField();
        sampleHBox.getChildren().add(zoomCenterXTextField);
        zoomCenterXTextField.setPrefWidth(80);
        Bindings.bindBidirectional(zoomCenterXTextField.textProperty(), zoomCenterX, INTEGER_FORMAT);

        sampleHBox.getChildren().add(new Label("Y:"));
        TextField zoomCenterYTextField = new TextField();
        sampleHBox.getChildren().add(zoomCenterYTextField);
        zoomCenterYTextField.setPrefWidth(80);
        Bindings.bindBidirectional(zoomCenterYTextField.textProperty(), zoomCenterY, INTEGER_FORMAT);

        sampleHBox.getChildren().add(new Label("Radius:"));
        Spinner<Number> sampleRadiusSpinner = new Spinner<>(1, 30, 5);
        sampleHBox.getChildren().add(sampleRadiusSpinner);
        sampleRadiusSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        sampleRadiusSpinner.setPrefWidth(80);
        sampleRadius.bind(sampleRadiusSpinner.valueProperty());

        // TODO show sample color

        rowIndex++;
      }

      {
        HBox fixPointToolbar = new HBox(4);
        mainGridPane.add(fixPointToolbar, 0, rowIndex, 4, 1);

        Button addFixPointButton = new Button("Add");
        fixPointToolbar.getChildren().add(addFixPointButton);
        addFixPointButton.setOnAction(event -> {
          int x = zoomCenterX.get();
          int y = zoomCenterY.get();
          double[] color = ImageUtil.averagePixel(inputDoubleImage, x, y, sampleRadius.get(), ColorModel.RGB);
          fixPoints.add(new FixPoint(x, y, new Color(color[0], color[1], color[2], 1.0)));
        });

        Button clearFixPointButton = new Button("Clear");
        fixPointToolbar.getChildren().add(clearFixPointButton);
        clearFixPointButton.setOnAction(event -> {
          fixPoints.clear();
        });
        rowIndex++;
      }

      {
        TableView<FixPoint> fixPointTableView = new TableView<>(fixPoints);
        mainGridPane.add(fixPointTableView, 0, rowIndex, 4, 1);
        fixPointTableView.setPlaceholder(new Label("Add points to define the background gradient."));
        fixPointTableView.setPrefHeight(150);
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
        addTableColumn(fixPointTableView, "X", 50, fixPoint -> {
          return new ReadOnlyIntegerWrapper(fixPoint.x);
        });
        addTableColumn(fixPointTableView, "Y", 50, fixPoint -> {
          return new ReadOnlyIntegerWrapper(fixPoint.y);
        });
        addTableColumn(fixPointTableView, "Color", 50, fixPoint -> {
          Rectangle rectangle = new Rectangle(15, 15);
          rectangle.setFill(fixPoint.color);
          return new ReadOnlyObjectWrapper<>(rectangle);
        });
        addTableColumn(fixPointTableView, "Red", 80, fixPoint -> {
          return new ReadOnlyStringWrapper(PERCENT_FORMAT.format(fixPoint.color.getRed()));
        });
        addTableColumn(fixPointTableView, "Green", 80, fixPoint -> {
          return new ReadOnlyStringWrapper(PERCENT_FORMAT.format(fixPoint.color.getGreen()));
        });
        addTableColumn(fixPointTableView, "Blue", 80, fixPoint -> {
          return new ReadOnlyStringWrapper(PERCENT_FORMAT.format(fixPoint.color.getBlue()));
        });
        rowIndex++;
      }

      {
        GridPane algorithmGridPane = new GridPane();
        mainGridPane.add(algorithmGridPane, rowIndex, 3, 1, 4);
        algorithmGridPane.setHgap(4);
        algorithmGridPane.setVgap(4);

        int algorithmRowIndex = 0;

        {
          algorithmGridPane.add(new Label("Point Finder:"), 0, algorithmRowIndex);
          ComboBox<PointFinderStrategy> pointFinderComboBox = new ComboBox<>(FXCollections
              .observableArrayList(PointFinderStrategy.values()));
          algorithmGridPane.add(pointFinderComboBox, 1, algorithmRowIndex);
          Bindings.bindBidirectional(pointFinderComboBox.valueProperty(), pointFinderStrategy);
          algorithmRowIndex++;
        }

        {
          algorithmGridPane.add(new Label("Interpolation Power:"), 0, algorithmRowIndex);
          TextField interpolationPowerTextField = new TextField();
          algorithmGridPane.add(interpolationPowerTextField, 1, algorithmRowIndex);
          Bindings.bindBidirectional(interpolationPowerTextField.textProperty(), interpolationPower, DOUBLE_FORMAT);
          algorithmRowIndex++;
        }

        {
          algorithmGridPane.add(new Label("Removal:"), 0, algorithmRowIndex);
          TextField removalFactorTextField = new TextField();
          algorithmGridPane.add(removalFactorTextField, 1, algorithmRowIndex);
          Bindings.bindBidirectional(removalFactorTextField.textProperty(), removalFactor, PERCENT_FORMAT);
          algorithmRowIndex++;
        }
      }

      {
        mainGridPane.add(new Label("Zoom:"), 0, rowIndex);
        mainGridPane.add(new Label("Output Preview:"), 1, rowIndex);
        rowIndex++;
        mainGridPane.add(withCrosshair(zoomInputImageView), 0, rowIndex);
        mainGridPane.add(withCrosshair(zoomPreviewImageView), 1, rowIndex);
        rowIndex++;
      }

      {
        mainGridPane.add(new Label("Gradient:"), 0, rowIndex);

        HBox hbox = new HBox(SPACING);
        mainGridPane.add(hbox, 1, rowIndex);
        hbox.getChildren().add(new Label("Delta:"));

        ComboBox<SampleChannel> zoomDeltaColorModelComboBox = new ComboBox<>(FXCollections
            .observableArrayList(SampleChannel.values()));
        hbox.getChildren().add(zoomDeltaColorModelComboBox);
        Bindings.bindBidirectional(zoomDeltaColorModelComboBox.valueProperty(), zoomDeltaSampleChannel);
        rowIndex++;

        mainGridPane.add(withCrosshair(zoomGradientImageView), 0, rowIndex);
        mainGridPane.add(withCrosshair(zoomDeltaImageView), 1, rowIndex);

        rowIndex++;
      }
    }

    setupZoomDragEvents(zoomInputImageView);
    setupZoomDragEvents(zoomPreviewImageView);
    setupZoomDragEvents(zoomGradientImageView);
    setupZoomDragEvents(zoomDeltaImageView);

    return mainBox;
  }

  private Node withZoomRectangle(ImageView imageView, Pane zoomRectanglePane) {
    Rectangle rectangle = new Rectangle();
    rectangle.setMouseTransparent(true);
    rectangle.strokeProperty().bind(crosshairColor);
    rectangle.setFill(Color.TRANSPARENT);

    zoomCenterX.addListener((observable, oldValue, newValue) -> {
      updateZoomRectangle(rectangle);
    });
    zoomCenterY.addListener((observable, oldValue, newValue) -> {
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
    double x = zoomCenterX.get() / inputImage.getWidth() * inputImageView.getBoundsInLocal().getWidth();
    double y = zoomCenterY.get() / inputImage.getHeight() * inputImageView.getBoundsInLocal().getHeight();
    rectangle.setX(x - width / 2);
    rectangle.setY(y - height / 2);
    rectangle.setWidth(width);
    rectangle.setHeight(height);
  }

  private Node withCrosshair(ImageView imageView) {
    IntegerBinding size = sampleRadius.multiply(2).add(1);

    Rectangle rectangle = new Rectangle();
    rectangle.setMouseTransparent(true);
    rectangle.widthProperty().bind(size);
    rectangle.heightProperty().bind(size);
    rectangle.strokeProperty().bind(crosshairColor);
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

      int zoomX = zoomCenterX.get() + (int) (deltaX);
      int zoomY = zoomCenterY.get() + (int) (deltaY);

      zoomX = Math.max(zoomX, 0);
      zoomY = Math.max(zoomY, 0);
      zoomX = Math.min(zoomX, (int) inputImage.getWidth() - 1);
      zoomY = Math.min(zoomY, (int) inputImage.getHeight() - 1);

      zoomCenterX.set(zoomX);
      zoomCenterY.set(zoomY);

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