package ch.obermuhlner.astro.javafx;

import ch.obermuhlner.astro.GradientRemover;
import ch.obermuhlner.astro.Point;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageCreator;
import ch.obermuhlner.astro.image.ImageQuality;
import ch.obermuhlner.astro.image.ImageReader;
import ch.obermuhlner.astro.image.ImageWriter;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

import javax.imageio.ImageIO;
import java.awt.image.*;
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

  private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("##0");

  private final Path homeDirectory = homeDirectory();

  private final GradientRemover gradientRemover = new GradientRemover();

  private final IntegerProperty zoomCenterX = new SimpleIntegerProperty();
  private final IntegerProperty zoomCenterY = new SimpleIntegerProperty();

  private File inputFile;

  private WritableImage inputImage;
  private ImageView inputImageView;

  private WritableImage zoomInputImage;
  private WritableDoubleImage zoomInputDoubleImage;
  private ImageView zoomInputImageView;

  private WritableImage zoomPreviewImage;
  private WritableDoubleImage zoomPreviewDoubleImage;
  private ImageView zoomPreviewImageView;

  private WritableImage zoomGradientImage;
  private WritableDoubleImage zoomGradientDoubleImage;
  private ImageView zoomGradientImageView;

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
        gradientRemover.setFixPoints(
            toPointList(fixPoints),
            new WritableDoubleImage(inputImage),
            3);
      }
    });
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
    DoubleImage inputImage = ImageReader.read(this.inputFile);
    DoubleImage outputImage = createOutputImage(inputImage);

    gradientRemover.removeGradient(inputImage, null, outputImage, 0, 0);

    ImageWriter.write(outputImage, outputFile);
  }

  private DoubleImage createOutputImage(DoubleImage inputImage) {
    int width = inputImage.getWidth();
    int height = inputImage.getHeight();
    return ImageCreator.create(width, height, ImageQuality.High);
  }

  private void loadImage(File file) throws IOException {
    BufferedImage bufferedImage = ImageIO.read(file);

    WritableImage writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
    PixelWriter pw = writableImage.getPixelWriter();
    for (int x = 0; x < bufferedImage.getWidth(); x++) {
      for (int y = 0; y < bufferedImage.getHeight(); y++) {
        pw.setArgb(x, y, bufferedImage.getRGB(x, y));
      }
    }

    inputImage = writableImage;
    inputImageView.setImage(writableImage);

    setZoom(bufferedImage.getWidth() / 2, bufferedImage.getHeight() / 2);
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

  private void updateZoom(int zoomX, int zoomY) {
    int zoomOffsetX = zoomX - ZOOM_WIDTH/2;
    int zoomOffsetY = zoomY - ZOOM_HEIGHT/2;

    zoomInputImage.getPixelWriter().setPixels(
        0,
        0,
        ZOOM_WIDTH,
        ZOOM_HEIGHT,
        inputImage.getPixelReader(),
        zoomOffsetX,
        zoomOffsetY);

    gradientRemover.removeGradient(
        zoomInputDoubleImage,
        zoomGradientDoubleImage,
        zoomPreviewDoubleImage,
        zoomOffsetX,
        zoomOffsetY);
  }

  private Node createEditor() {
    VBox box = new VBox(4);

    zoomInputImage = new WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT);
    zoomInputDoubleImage = new WritableDoubleImage(zoomInputImage);
    zoomInputImageView = new ImageView(zoomInputImage);

    zoomPreviewImage = new WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT);
    zoomPreviewDoubleImage = new WritableDoubleImage(zoomPreviewImage);
    zoomPreviewImageView = new ImageView(zoomPreviewImage);

    zoomGradientImage = new WritableImage(ZOOM_WIDTH, ZOOM_HEIGHT);
    zoomGradientDoubleImage = new WritableDoubleImage(zoomGradientImage);
    zoomGradientImageView = new ImageView(zoomGradientImage);

    GridPane gridPane = new GridPane();
    box.getChildren().add(gridPane);
    gridPane.setHgap(4);
    gridPane.setVgap(4);

    int rowIndex = 0;

    gridPane.add(new Label("X:"), 0, rowIndex);
    TextField zoomCenterXTextField = new TextField();
    gridPane.add(zoomCenterXTextField, 1, rowIndex);
    Bindings.bindBidirectional(zoomCenterXTextField.textProperty(), zoomCenterX, INTEGER_FORMAT);
    rowIndex++;

    gridPane.add(new Label("Y:"), 0, rowIndex);
    TextField zoomCenterYTextField = new TextField();
    gridPane.add(zoomCenterYTextField, 1, rowIndex);
    Bindings.bindBidirectional(zoomCenterYTextField.textProperty(), zoomCenterY, INTEGER_FORMAT);
    rowIndex++;

    gridPane.add(new Label("Zoom:"), 0, rowIndex);
    gridPane.add(zoomInputImageView, 1, rowIndex);
    gridPane.add(new Label("Preview:"), 2, rowIndex);
    gridPane.add(zoomPreviewImageView, 3, rowIndex);
    rowIndex++;

    gridPane.add(new Label("Gradient:"), 0, rowIndex);
    gridPane.add(zoomGradientImageView, 1, rowIndex);
    rowIndex++;

    VBox fixPointToolbar = new VBox(4);
    gridPane.add(fixPointToolbar, 0, rowIndex, 4, 1);
    Button addFixPointButton = new Button("Add");
    fixPointToolbar.getChildren().add(addFixPointButton);
    addFixPointButton.setOnAction(event -> {
      fixPoints.add(new FixPoint(zoomCenterX.get(), zoomCenterY.get()));
    });
    rowIndex++;

    TableView<FixPoint> fixPointTableView = new TableView<>(fixPoints);
    gridPane.add(fixPointTableView, 0, rowIndex, 4, 1);
    fixPointTableView.setPrefHeight(200);
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


    setupZoomDragEvents(zoomInputImageView);
    setupZoomDragEvents(zoomPreviewImageView);
    setupZoomDragEvents(zoomGradientImageView);

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