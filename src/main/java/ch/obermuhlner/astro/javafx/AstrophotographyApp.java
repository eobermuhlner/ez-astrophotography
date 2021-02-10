package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.GradientRemover;
import ch.obermuhlner.astro.Point;
import ch.obermuhlner.astro.image.ArrayDoubleImage;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
import java.util.List;
import java.util.function.Function;

public class AstrophotographyApp extends Application {

  private static final int IMAGE_WIDTH = 800;
  private static final int IMAGE_HEIGHT = 600;

  private static final int ZOOM_WIDTH = 200;
  private static final int ZOOM_HEIGHT = 200;

  private static final boolean ACCURATE_PREVIEW = true;
  private static final boolean ACCURATE_PREVIEW_USING_WRITE_THROUGH = true;

  private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("##0");
  private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("##0.000");
  private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("##0.000%");

  private final Path homeDirectory = homeDirectory();

  private final GradientRemover gradientRemover = new GradientRemover();

  private final IntegerProperty zoomCenterX = new SimpleIntegerProperty();
  private final IntegerProperty zoomCenterY = new SimpleIntegerProperty();

  private final ObjectProperty<ColorModel> zoomDeltaColorModel = new SimpleObjectProperty<>(ColorModel.HSV);
  private final IntegerProperty zoomDeltaSampleIndex = new SimpleIntegerProperty(ColorModel.V);

  private final IntegerProperty sampleRadius = new SimpleIntegerProperty(3);

  private final DoubleProperty interpolationPower = new SimpleDoubleProperty(3.0);
  private final DoubleProperty removalFactor = new SimpleDoubleProperty(1.0);

  private File inputFile;

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

  private final ObservableList<FixPoint> fixPoints = FXCollections.observableArrayList();

  @Override
  public void start(Stage primaryStage) {
    Group root = new Group();
    Scene scene = new Scene(root);

    BorderPane borderPane = new BorderPane();
    root.getChildren().add(borderPane);

    Node toolbar = createToolbar(primaryStage);
    borderPane.setTop(toolbar);

    Node imageViewer = createImageViewer();
    borderPane.setCenter(imageViewer);

    Node editor = createEditor();
    borderPane.setRight(editor);

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
  }

  private void updateFixPoints() {
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

    return box;
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

    inputImage = new WritableImage(inputDoubleImage.getWidth(), inputDoubleImage.getHeight());
    double[] rgb = new double[3];
    PixelWriter pw = inputImage.getPixelWriter();
    for (int x = 0; x < inputDoubleImage.getWidth(); x++) {
      for (int y = 0; y < inputDoubleImage.getHeight(); y++) {
        inputDoubleImage.getPixel(x, y, ColorModel.RGB, rgb);
        pw.setArgb(x, y, 0xff000000 | ColorUtil.toIntRGB(rgb));
      }
    }

    inputImageView.setImage(inputImage);

    setZoom(inputDoubleImage.getWidth() / 2, inputDoubleImage.getHeight() / 2);
  }

  private List<Point> toPointList(ObservableList<FixPoint> fixPoints) {
    List<Point> points = new ArrayList<>();
    for (FixPoint fixPoint : fixPoints) {
      points.add(new Point(fixPoint.x.get(), fixPoint.y.get()));
    }
    return points;
  }

  private Node createImageViewer() {
    VBox box = new VBox(2);

    inputImageView = new ImageView();
    box.getChildren().add(inputImageView);

    inputImageView.setPreserveRatio(true);
    inputImageView.setFitWidth(IMAGE_WIDTH);
    inputImageView.setFitHeight(IMAGE_HEIGHT);

    setMouseDragEvents(inputImageView, event -> {
      double imageViewWidth = inputImageView.getBoundsInLocal().getWidth();
      double imageViewHeight = inputImageView.getBoundsInLocal().getHeight();
      int zoomX = (int) (event.getX() * inputImage.getWidth() / imageViewWidth);
      int zoomY = (int) (event.getY() * inputImage.getHeight() / imageViewHeight);

      zoomX = Math.max(zoomX, ZOOM_WIDTH/2);
      zoomY = Math.max(zoomY, ZOOM_HEIGHT/2);
      zoomX = Math.min(zoomX, (int) inputImage.getWidth() - ZOOM_WIDTH/2);
      zoomY = Math.min(zoomY, (int) inputImage.getHeight() - ZOOM_HEIGHT/2);

      setZoom(zoomX, zoomY);
    });

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

    if (ACCURATE_PREVIEW) {
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
    }

    if (!ACCURATE_PREVIEW_USING_WRITE_THROUGH) {
      int zoomWidth = ZOOM_WIDTH;
      int zoomHeight = ZOOM_HEIGHT;

      if (zoomOffsetX < ZOOM_WIDTH) {
        zoomWidth = ZOOM_WIDTH - zoomOffsetX;
        zoomOffsetX = 0;
      } else if (zoomOffsetX > inputDoubleImage.getWidth() - ZOOM_WIDTH) {
        zoomWidth = inputDoubleImage.getWidth() - zoomOffsetX;
      }
      if (zoomOffsetY < ZOOM_HEIGHT) {
        zoomHeight = ZOOM_HEIGHT - zoomOffsetY;
        zoomOffsetY = 0;
      } else if (zoomOffsetY > inputDoubleImage.getHeight() - ZOOM_HEIGHT) {
        zoomHeight = inputDoubleImage.getHeight() - zoomOffsetY;
      }

      zoomInputImage.getPixelWriter().setPixels(
          0,
          0,
          zoomWidth,
          zoomHeight,
          inputImage.getPixelReader(),
          zoomOffsetX,
          zoomOffsetY
      );
      // TODO fill outside area
    }

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
      if (ACCURATE_PREVIEW_USING_WRITE_THROUGH) {
        zoomInputDoubleImage = new WriteThroughArrayDoubleImage(new JavaFXWritableDoubleImage(zoomInputImage), ColorModel.RGB);
      } else {
        zoomInputDoubleImage = new ArrayDoubleImage(ZOOM_WIDTH, ZOOM_HEIGHT, ColorModel.RGB);
      }
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
    gridPane.add(zoomInputImageView, 1, rowIndex);
    gridPane.add(new Label("Preview:"), 2, rowIndex);
    gridPane.add(zoomPreviewImageView, 3, rowIndex);
    rowIndex++;

    gridPane.add(new Label("Gradient:"), 0, rowIndex);
    gridPane.add(zoomGradientImageView, 1, rowIndex);
    gridPane.add(new Label("Delta:"), 2, rowIndex);
    gridPane.add(zoomDeltaImageView, 3, rowIndex);

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