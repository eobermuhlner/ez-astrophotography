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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import javafx.scene.layout.BorderPane;
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

  private static final int IMAGE_WIDTH = 800;
  private static final int IMAGE_HEIGHT = 600;

  private static final int ZOOM_WIDTH = 200;
  private static final int ZOOM_HEIGHT = 200;

  private static final boolean ACCURATE_PREVIEW = true;

  private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("##0");
  private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("##0.000");
  private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("##0.000%");

  private final Path homeDirectory = homeDirectory();

  private final GradientRemover gradientRemover = new GradientRemover();

  private final IntegerProperty zoomCenterX = new SimpleIntegerProperty();
  private final IntegerProperty zoomCenterY = new SimpleIntegerProperty();

  private final ObjectProperty<ColorModel> zoomDeltaColorModel = new SimpleObjectProperty<>(ColorModel.HSV);
  private final IntegerProperty zoomDeltaSampleIndex = new SimpleIntegerProperty(ColorModel.V);

  private final IntegerProperty sampleRadius = new SimpleIntegerProperty();

  private final ObjectProperty<PointFinderStrategy> pointFinderStrategy = new SimpleObjectProperty<>();
  private final DoubleProperty interpolationPower = new SimpleDoubleProperty();
  private final DoubleProperty removalFactor = new SimpleDoubleProperty();

  private final List<Color> crosshairColors = Arrays.asList(Color.YELLOW, Color.RED, Color.GREEN, Color.BLUE, Color.TRANSPARENT);
  private final ObjectProperty<Color> crosshairColor = new SimpleObjectProperty<>(crosshairColors.get(0));
  private final ObjectProperty<Color> fixPointColor = new SimpleObjectProperty<>(crosshairColors.get(1));

  private File inputFile;

  private Pane fixPointPane;

  private WritableImage inputImage;
  private DoubleImage inputDoubleImage;
  private ImageView inputImageView;

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

  private WritableImage gradientImage;
  private DoubleImage gradientDoubleImage;
  private ImageView gradientImageView;

  private WritableImage outputImage;
  private DoubleImage outputDoubleImage;
  private ImageView outputImageView;

  private final ObservableList<FixPoint> fixPoints = FXCollections.observableArrayList();

  @Override
  public void start(Stage primaryStage) {
    Group root = new Group();
    Scene scene = new Scene(root);

    BorderPane borderPane = new BorderPane();
    root.getChildren().add(borderPane);

    borderPane.setTop(createToolbar(primaryStage));

    TabPane imageTabPane = new TabPane();
    borderPane.setCenter(imageTabPane);
    imageTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    Tab inputTab = new Tab("Input", createInputImageViewer());
    imageTabPane.getTabs().add(inputTab);
    Tab gradientTab = new Tab("Gradient", createGradientImageViewer());
    imageTabPane.getTabs().add(gradientTab);
    Tab outputTab = new Tab("Output", createOutputImageViewer());
    imageTabPane.getTabs().add(outputTab);
    borderPane.setRight(createEditor());

    imageTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != inputTab) {
        gradientRemover.removeGradient(inputDoubleImage, gradientDoubleImage, outputDoubleImage);
      }
    });

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
    zoomDeltaColorModel.addListener((observable, oldValue, newValue) -> {
      updateZoom();
    });
    zoomDeltaSampleIndex.addListener((observable, oldValue, newValue) -> {
      updateZoom();
    });

    initializeValues();
  }

  private void initializeValues() {
    sampleRadius.set(3);
    pointFinderStrategy.set(PointFinderStrategy.All);
    interpolationPower.set(3.0);
    removalFactor.set(1.0);
  }

  private void updateFixPoints() {
    fixPointPane.getChildren().clear();
    for (FixPoint fixPoint : fixPoints) {
      Circle circle = new Circle(3);
      circle.setFill(Color.TRANSPARENT);
      circle.strokeProperty().bind(fixPointColor);
      double x = fixPoint.x.get() / inputImage.getWidth() * inputImageView.getBoundsInLocal().getWidth();
      double y = fixPoint.y.get() / inputImage.getHeight() * inputImageView.getBoundsInLocal().getHeight();
      circle.setCenterX(x);
      circle.setCenterY(y);
      fixPointPane.getChildren().add(circle);
    }

    gradientRemover.setFixPoints(
        toPointList(fixPoints),
        inputDoubleImage,
        sampleRadius.get());

    updateZoom();
  }

  private Node createToolbar(Stage stage) {
    HBox box = new HBox(2);

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

    setZoom(width / 2, height / 2);

    gradientImage = new WritableImage(width, height);
    gradientDoubleImage = new JavaFXWritableDoubleImage(gradientImage);
    gradientImageView.setImage(gradientImage);

    outputImage = new WritableImage(width, height);
    outputDoubleImage = new JavaFXWritableDoubleImage(outputImage);
    outputImageView.setImage(outputImage);
  }

  private List<Point> toPointList(ObservableList<FixPoint> fixPoints) {
    List<Point> points = new ArrayList<>();
    for (FixPoint fixPoint : fixPoints) {
      points.add(new Point(fixPoint.x.get(), fixPoint.y.get()));
    }
    return points;
  }

  private Node createInputImageViewer() {
    VBox box = new VBox(2);

    fixPointPane = new Pane();
    fixPointPane.setMouseTransparent(true);
    inputImageView = new ImageView();
    box.getChildren().add(withZoomRectangle(inputImageView, fixPointPane));

    inputImageView.setPreserveRatio(true);
    inputImageView.setFitWidth(IMAGE_WIDTH);
    inputImageView.setFitHeight(IMAGE_HEIGHT);

    setMouseDragEvents(inputImageView, event -> {
      double imageViewWidth = inputImageView.getBoundsInLocal().getWidth();
      double imageViewHeight = inputImageView.getBoundsInLocal().getHeight();
      int zoomX = (int) (event.getX() * inputImage.getWidth() / imageViewWidth);
      int zoomY = (int) (event.getY() * inputImage.getHeight() / imageViewHeight);

      zoomX = Math.max(zoomX, 0);
      zoomY = Math.max(zoomY, 0);
      zoomX = Math.min(zoomX, (int) inputImage.getWidth());
      zoomY = Math.min(zoomY, (int) inputImage.getHeight());

      setZoom(zoomX, zoomY);
    });

    return box;
  }

  private Node createGradientImageViewer() {
    VBox box = new VBox(2);

    gradientImageView = new ImageView();
    box.getChildren().add(gradientImageView);

    gradientImageView.setPreserveRatio(true);
    gradientImageView.setFitWidth(IMAGE_WIDTH);
    gradientImageView.setFitHeight(IMAGE_HEIGHT);

    return box;
  }

  private Node createOutputImageViewer() {
    VBox box = new VBox(2);

    outputImageView = new ImageView();
    box.getChildren().add(outputImageView);

    outputImageView.setPreserveRatio(true);
    outputImageView.setFitWidth(IMAGE_WIDTH);
    outputImageView.setFitHeight(IMAGE_HEIGHT);

    return box;
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
        zoomDeltaColorModel.get(),
        zoomDeltaSampleIndex.get());
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
    VBox box = new VBox(4);

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

    GridPane gridPane = new GridPane();
    box.getChildren().add(gridPane);
    gridPane.setHgap(4);
    gridPane.setVgap(4);

    int rowIndex = 0;

    gridPane.add(new Label("X:"), 0, rowIndex);
    TextField zoomCenterXTextField = new TextField();
    gridPane.add(zoomCenterXTextField, 1, rowIndex);
    Bindings.bindBidirectional(zoomCenterXTextField.textProperty(), zoomCenterX, INTEGER_FORMAT);

    gridPane.add(new Label("Y:"), 2, rowIndex);
    TextField zoomCenterYTextField = new TextField();
    gridPane.add(zoomCenterYTextField, 3, rowIndex);
    Bindings.bindBidirectional(zoomCenterYTextField.textProperty(), zoomCenterY, INTEGER_FORMAT);
    rowIndex++;

    gridPane.add(new Label("Zoom:"), 0, rowIndex);
    gridPane.add(withCrosshair(zoomInputImageView), 1, rowIndex);
    gridPane.add(new Label("Preview:"), 2, rowIndex);
    gridPane.add(withCrosshair(zoomPreviewImageView), 3, rowIndex);
    rowIndex++;

    gridPane.add(new Label("Gradient:"), 0, rowIndex);
    gridPane.add(withCrosshair(zoomGradientImageView), 1, rowIndex);
    gridPane.add(new Label("Delta:"), 2, rowIndex);
    gridPane.add(withCrosshair(zoomDeltaImageView), 3, rowIndex);

    ComboBox<ColorModel> zoomDeltaColorModelComboBox = new ComboBox<>(FXCollections.observableArrayList(ColorModel.values()));
    gridPane.add(zoomDeltaColorModelComboBox, 4, rowIndex);
    Bindings.bindBidirectional(zoomDeltaColorModelComboBox.valueProperty(), zoomDeltaColorModel);

    ComboBox<Number> zoomDeltaSampleIndexComboBox = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2));
    gridPane.add(zoomDeltaSampleIndexComboBox, 5, rowIndex);
    Bindings.bindBidirectional(zoomDeltaSampleIndexComboBox.valueProperty(), zoomDeltaSampleIndex);
    rowIndex++;

    HBox fixPointToolbar = new HBox(4);
    gridPane.add(fixPointToolbar, 0, rowIndex, 4, 1);
    Button addFixPointButton = new Button("Add");
    fixPointToolbar.getChildren().add(addFixPointButton);
    addFixPointButton.setOnAction(event -> {
      fixPoints.add(new FixPoint(zoomCenterX.get(), zoomCenterY.get()));
    });
    Button clearFixPointButton = new Button("Clear");
    fixPointToolbar.getChildren().add(clearFixPointButton);
    clearFixPointButton.setOnAction(event -> {
      fixPoints.clear();
    });
    rowIndex++;

    TableView<FixPoint> fixPointTableView = new TableView<>(fixPoints);
    gridPane.add(fixPointTableView, 0, rowIndex, 4, 1);
    fixPointTableView.setPlaceholder(new Label("Add points to define the background gradient."));
    fixPointTableView.setPrefHeight(150);
    fixPointTableView.setRowFactory(new Callback<TableView<FixPoint>, TableRow<FixPoint>>() {
      @Override
      public TableRow<FixPoint> call(TableView<FixPoint> param) {
        TableRow<FixPoint> tableRow = new TableRow<>();
        MenuItem gotoMenuItem = new MenuItem("Go To");
        gotoMenuItem.setOnAction(event -> {
          setZoom(tableRow.getItem().x.get(), tableRow.getItem().y.get());
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
      return new ReadOnlyStringWrapper(String.valueOf(fixPoint.xProperty().get()));
    });
    addTableColumn(fixPointTableView, "Y", 50, fixPoint -> {
      return new ReadOnlyStringWrapper(String.valueOf(fixPoint.yProperty().get()));
    });
    rowIndex++;

    gridPane.add(new Label("Sample Radius:"), 0, rowIndex);
    TextField sampleRadiusTextField = new TextField();
    gridPane.add(sampleRadiusTextField, 1, rowIndex);
    Bindings.bindBidirectional(sampleRadiusTextField.textProperty(), sampleRadius, INTEGER_FORMAT);
    rowIndex++;

    gridPane.add(new Label("Point Finder:"), 0, rowIndex);
    ComboBox<PointFinderStrategy> pointFinderComboBox = new ComboBox<>(FXCollections.observableArrayList(PointFinderStrategy.values()));
    gridPane.add(pointFinderComboBox, 1, rowIndex);
    Bindings.bindBidirectional(pointFinderComboBox.valueProperty(), pointFinderStrategy);
    rowIndex++;

    gridPane.add(new Label("Interpolation Power:"), 0, rowIndex);
    TextField interpolationPowerTextField = new TextField();
    gridPane.add(interpolationPowerTextField, 1, rowIndex);
    Bindings.bindBidirectional(interpolationPowerTextField.textProperty(), interpolationPower, DOUBLE_FORMAT);
    rowIndex++;

    gridPane.add(new Label("Removal:"), 0, rowIndex);
    TextField removalFactorTextField = new TextField();
    gridPane.add(removalFactorTextField, 1, rowIndex);
    Bindings.bindBidirectional(removalFactorTextField.textProperty(), removalFactor, PERCENT_FORMAT);
    rowIndex++;

    setupZoomDragEvents(zoomInputImageView);
    setupZoomDragEvents(zoomPreviewImageView);
    setupZoomDragEvents(zoomGradientImageView);
    setupZoomDragEvents(zoomDeltaImageView);

    return box;
  }

  private Node withZoomRectangle(ImageView imageView, Pane additionalPane) {
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

    return new Pane(imageView, rectangle, additionalPane);
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

      zoomX = Math.max(zoomX, ZOOM_WIDTH/2);
      zoomY = Math.max(zoomY, ZOOM_HEIGHT/2);
      zoomX = Math.min(zoomX, (int) inputImage.getWidth() - ZOOM_WIDTH/2);
      zoomY = Math.min(zoomY, (int) inputImage.getHeight() - ZOOM_HEIGHT/2);

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
    private final IntegerProperty x = new SimpleIntegerProperty();
    private final IntegerProperty y = new SimpleIntegerProperty();

    public FixPoint(int x, int y) {
      this.x.set(x);
      this.y.set(y);
    }

    public IntegerProperty xProperty() {
      return x;
    };

    public IntegerProperty yProperty() {
      return y;
    };
  }
}