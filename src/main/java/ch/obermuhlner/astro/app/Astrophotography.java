package ch.obermuhlner.astro.app;

import ch.obermuhlner.astro.app.CommandParser.Command;
import ch.obermuhlner.astro.app.CommandParser.Option;
import ch.obermuhlner.astro.gradient.Point;
import ch.obermuhlner.astro.gradient.filter.Filter;
import ch.obermuhlner.astro.gradient.filter.GaussianBlurFilter;
import ch.obermuhlner.astro.gradient.filter.GradientInterpolationFilter;
import ch.obermuhlner.astro.gradient.filter.HorizontalMedianFilter;
import ch.obermuhlner.astro.gradient.filter.MedianFilter;
import ch.obermuhlner.astro.gradient.filter.PseudoMedianFilter;
import ch.obermuhlner.astro.gradient.filter.VerticalMedianFilter;
import ch.obermuhlner.astro.gradient.operation.ImageOperation;
import ch.obermuhlner.astro.gradient.operation.SubtractLinearImageOperation;
import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageCreator;
import ch.obermuhlner.astro.image.ImageQuality;
import ch.obermuhlner.astro.image.ImageReader;
import ch.obermuhlner.astro.image.ImageWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class Astrophotography {

  public static void mainTest(String[] args) throws IOException, ScriptException {
    runTest();
  }

  public static void main(String[] args) throws IOException, ScriptException {
    String outputFilePrefix = "output_";
    List<File> inputFiles = new ArrayList<>();
    List<File> outputFiles = new ArrayList<>();

    AtomicReference<Filter> gradientFilter = new AtomicReference<>(new GaussianBlurFilter(100, ColorModel.RGB));
    AtomicReference<ImageOperation> subtractOperation = new AtomicReference<>(new SubtractLinearImageOperation());
    List<Point> gradientPoints = new ArrayList<>();
    AtomicInteger sampleRadius = new AtomicInteger(3);

    CommandParser commandParser = new CommandParser();

    commandParser.add(
        new Command("script", 1),
        (commandArguments, optionsWithArguments) -> {
          String[] script = loadScript(new File(commandArguments.arguments.get(0)));
          commandParser.parse(script);
        }
    );
    commandParser.add(
        new Command("input", 1),
        (commandArguments, optionsWithArguments) -> {
          inputFiles.add(new File(commandArguments.arguments.get(0)));
        }
    );
    commandParser.add(
        new Command("batch", 0,
            new Option("file", 1)),
        (commandArguments, optionsWithArguments) -> {
          optionsWithArguments.handleOption("file", arguments -> {
            inputFiles.add(new File(arguments.get(0)));
          });
        }
    );
    commandParser.add(
        new Command("gradient", 0,
            new Option("point", 1),
            new Option("sampleradius", 1),
            new Option("interpolationpower", 1)
        ),
        (commandArguments, optionsWithArguments) -> {
          GradientInterpolationFilter gradientInterpolationFilter = new GradientInterpolationFilter();
          optionsWithArguments.handleOption("point", arguments -> {
            gradientPoints.add(toPoint(arguments.get(0)));
          });
          optionsWithArguments.handleOption("interpolationpower", arguments -> {
            gradientInterpolationFilter.setInterpolationPower(Double.parseDouble(arguments.get(0)));
          });
          optionsWithArguments.handleOption("sampleradius", arguments -> {
            sampleRadius.set(Integer.parseInt(arguments.get(0)));
          });

          gradientFilter.set(gradientInterpolationFilter);
        }
    );
    commandParser.add(
        new Command("output", 1),
        (commandArguments, optionsWithArguments) -> {
          outputFiles.add(new File(commandArguments.arguments.get(0)));
        }
    );

    commandParser.parse(new String[] {
        "input", "images/Autosave001.tif",
//        "input", "images/inputs/Autosave001_small_compress0.png",
//        "median-blur", "10",
//        "gaussian-blur", "50",
//        "gradient", "--point", "100,100", "--point", "-100,-100",
        "output", "images/Test.png"

    });
    //commandParser.parse(args);

    for (int f = 0; f < inputFiles.size(); f++) {
      File inputFile = inputFiles.get(f);
      File outputFile;
      if (f < outputFiles.size()) {
        outputFile = outputFiles.get(f);
      } else {
        outputFile = new File(inputFile.getParent(), outputFilePrefix + inputFile.getName());
      }

      System.out.println("Load " + inputFile);

      DoubleImage inputImage = ImageReader.read(inputFile, ImageQuality.High);

      if (gradientFilter.get() instanceof GradientInterpolationFilter) {
        GradientInterpolationFilter gradientInterpolationFilter = (GradientInterpolationFilter) gradientFilter.get();
        gradientInterpolationFilter.setFixPoints(correctFixPoints(gradientPoints, inputImage), inputImage, sampleRadius.get());
      }

      System.out.println("Create gradient " + gradientFilter.get());
      DoubleImage gradientImage = gradientFilter.get().filter(inputImage);

      System.out.println("Subtract gradient " + subtractOperation.get());
      DoubleImage outputImage = ImageCreator.create(inputImage.getWidth(), inputImage.getHeight(), ImageQuality.High);
      subtractOperation.get().operation(inputImage, gradientImage, outputImage);

      System.out.println("Save " + outputFile);
      ImageWriter.write(outputImage, outputFile);

      System.out.println("Finished " + inputFile + " -> " + outputFile);
    }
  }

  private static void autoSetFixPoints(GradientInterpolationFilter gradientInterpolationFilter, DoubleImage image) {
    int sampleWidth = image.getWidth() / 5;
    int sampleHeight = image.getHeight() / 5;

    int x1 = image.getWidth() / 2;
    int y1 = sampleHeight / 2;
    double[] color1 = image.medianPixel(x1, y1, sampleWidth, sampleHeight, ColorModel.RGB, null);
    System.out.println("Auto median pixel1: " + Arrays.toString(color1));

    int x2 = sampleWidth / 2;
    int y2 = image.getHeight() - sampleHeight / 2;
    double[] color2 = image.medianPixel(x2, y2, sampleWidth, sampleHeight, ColorModel.RGB, null);
    System.out.println("Auto median pixel2: " + Arrays.toString(color2));

    int x3 = image.getWidth() - sampleWidth / 2;
    int y3 = image.getHeight() - sampleHeight / 2;
    double[] color3 = image.medianPixel(x3, y3, sampleWidth, sampleHeight, ColorModel.RGB, null);
    System.out.println("Auto median pixel3: " + Arrays.toString(color3));

    gradientInterpolationFilter.setFixPoints(
        Arrays.asList(new Point(x1, y1), new Point(x2, y2), new Point(x3, y3)),
        Arrays.asList(color1, color2, color3));
  }

  private static String[] loadScript(File file) {
    try {
      String script = Files.readString(file.toPath());
      return script.split("\\s+");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<Point> correctFixPoints(List<Point> fixPoints, DoubleImage inputImage) {
    List<Point> result = new ArrayList<>();

    for (Point fixPoint : fixPoints) {
      if (fixPoint.x < 0) {
        fixPoint = new Point(inputImage.getWidth() + fixPoint.x, fixPoint.y);
      }
      if (fixPoint.y < 0) {
        fixPoint = new Point(fixPoint.x, inputImage.getHeight() + fixPoint.y);
      }

      result.add(fixPoint);
    }

    return result;
  }

  private static Point toPoint(String string) {
    String[] split = string.split(Pattern.quote(","));
    return new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
  }

  private static void runTest() throws IOException {
    var input = loadImage("images/Autosave001.tif");
    var gradient = gaussianBlur(input, 20);
    saveImage(gradient, "images/TestGradient.png");
  }

  private static void runScript() throws ScriptException {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("jshell");

    String script = "" +
        "import static ch.obermuhlner.astro.app.Astrophotography.*;" +
        "var input = loadImage(\"images/Autosave001.tif\");" +
        "var gradient = gaussianBlur(input, 200);" +
        "var output = subtractLinear(input, gradient);" +
        "saveImage(output, \"images/Test.png\");";

    engine.eval(script);
  }

  public static DoubleImage loadImage(String filename) throws IOException {
    return ImageReader.read(new File(filename), ImageQuality.High);
  }
  public static void saveImage(DoubleImage image, String filename) throws IOException {
    ImageWriter.write(image, new File(filename));
  }
  public static DoubleImage gaussianBlur(DoubleImage image, int radius) {
    return new GaussianBlurFilter(radius, ColorModel.RGB).filter(image);
  }
  public static DoubleImage medianBlur(DoubleImage image, int radius) {
    return new MedianFilter(radius, ColorModel.RGB).filter(image);
  }
  public static DoubleImage horizontalMedianFilter(DoubleImage image, int radius) {
    return new HorizontalMedianFilter(radius, ColorModel.RGB).filter(image);
  }
  public static DoubleImage verticalMedianFilter(DoubleImage image, int radius) {
    return new VerticalMedianFilter(radius, ColorModel.RGB).filter(image);
  }
  public static DoubleImage pseudoMedianFilter(DoubleImage image, int radius) {
    return new PseudoMedianFilter(radius, ColorModel.RGB).filter(image);
  }
  public static DoubleImage gradient(DoubleImage image) {
    GradientInterpolationFilter gradientInterpolationFilter = new GradientInterpolationFilter();
    autoSetFixPoints(gradientInterpolationFilter, image);
    return gradientInterpolationFilter.filter(image);
  }
  public static DoubleImage subtractLinear(DoubleImage image1, DoubleImage image2) {
    return new SubtractLinearImageOperation().operation(image1, image2);
  }

}
