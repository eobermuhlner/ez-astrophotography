package ch.obermuhlner.astro.app;

import ch.obermuhlner.astro.app.CommandParser.Command;
import ch.obermuhlner.astro.app.CommandParser.Option;
import ch.obermuhlner.astro.gradient.GradientRemover;
import ch.obermuhlner.astro.gradient.Point;
import ch.obermuhlner.astro.gradient.correction.LinearSampleSubtraction;
import ch.obermuhlner.astro.gradient.correction.SimpleSampleSubtraction;
import ch.obermuhlner.astro.gradient.correction.SplineSampleSubtraction;
import ch.obermuhlner.astro.gradient.filter.BoxBlurFilter;
import ch.obermuhlner.astro.gradient.filter.Filter;
import ch.obermuhlner.astro.gradient.filter.GaussianBlurFilter;
import ch.obermuhlner.astro.image.color.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageCreator;
import ch.obermuhlner.astro.image.ImageQuality;
import ch.obermuhlner.astro.image.ImageReader;
import ch.obermuhlner.astro.image.ImageUtil;
import ch.obermuhlner.astro.image.ImageWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Astrophotography {

  public static void main(String[] args) throws IOException {
    GradientRemover gradientRemover = new GradientRemover();
    List<Filter> filters = new ArrayList<>();

    String outputFilePrefix = "output_";
    List<File> inputFiles = new ArrayList<>();
    List<File> outputFiles = new ArrayList<>();
    List<Point> gradientPoints = new ArrayList<>();
    AtomicInteger sampleRadius = new AtomicInteger(5);

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
            new Option("removal", 1),
            new Option("interpolationpower", 1)
        ),
        (commandArguments, optionsWithArguments) -> {
          optionsWithArguments.handleOption("point", arguments -> {
            gradientPoints.add(toPoint(arguments.get(0)));
          });
          optionsWithArguments.handleOption("sampleradius", arguments -> {
            sampleRadius.set(Integer.parseInt(arguments.get(0)));
          });
          optionsWithArguments.handleOption("removal", arguments -> {
            gradientRemover.setRemovalFactor(Double.parseDouble(arguments.get(0)));
          });
          optionsWithArguments.handleOption("interpolationpower", arguments -> {
            gradientRemover.setInterpolationPower(Double.parseDouble(arguments.get(0)));
          });
        }
    );
    commandParser.add(
        new Command("filter", 0,
            new Option("box-blur", 1),
            new Option("gaussian-blur", 1)
        ),
        (commandArguments, optionsWithArguments) -> {
          optionsWithArguments.handleOption("box-blur", arguments -> {
            filters.add(new BoxBlurFilter(Integer.parseInt(arguments.get(0)), ColorModel.RGB));
          });
          optionsWithArguments.handleOption("gaussian-blur", arguments -> {
            filters.add(new GaussianBlurFilter(Integer.parseInt(arguments.get(0)), ColorModel.RGB));
          });
        }
    );
    commandParser.add(
        new Command("curve-subtract", 0),
        (commandArguments, optionsWithArguments) -> {
          gradientRemover.setSampleSubtraction(new SimpleSampleSubtraction());
        }
    );
    commandParser.add(
        new Command("curve-linear", 0),
        (commandArguments, optionsWithArguments) -> {
          gradientRemover.setSampleSubtraction(new LinearSampleSubtraction());
        }
    );
    commandParser.add(
        new Command("curve-spline", 0,
            new Option("gradient-factor", 1),
            new Option("stretch", 2)
        ),
        (commandArguments, optionsWithArguments) -> {
          double gradientFactor = Double.parseDouble(optionsWithArguments.getOptionArguments("gradient-factor", "0.01").get(0));
          List<String> stretchArgs = optionsWithArguments.getOptionArguments("stretch");
          if (stretchArgs.isEmpty()) {
            double stretchX = Double.parseDouble(stretchArgs.get(0));
            double stretchY = Double.parseDouble(stretchArgs.get(1));
            gradientRemover.setSampleSubtraction(new SplineSampleSubtraction(gradientFactor, stretchX, stretchY));
          } else {
            gradientRemover.setSampleSubtraction(new SplineSampleSubtraction(gradientFactor));
          }
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
//        "gradient", "--point", "100,100", "--point", "-100,-100",
        "gradient",
//        "curve-linear",
//        "curve-spline", "--gradient-factor", "0.02", "--stretch", "0.6", "0.9",
//        "filter", "--gaussian-blur", "50",
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
      DoubleImage outputImage = ImageCreator.create(inputImage.getWidth(), inputImage.getHeight(), ImageQuality.High);

      if (!filters.isEmpty()) {
        for (Filter filter : filters) {
          System.out.println("Process filter " + filter);
          filter.filter(inputImage, outputImage);
        }
      } else {
        if (gradientPoints.isEmpty()) {
          autoSetFixPoints(gradientRemover, inputImage);
        } else {
          List<Point> correctFixPoints = correctFixPoints(gradientPoints, inputImage);
          System.out.println("Set fix points " + correctFixPoints);
          gradientRemover.setFixPoints(correctFixPoints, inputImage, sampleRadius.get());
        }

        System.out.println("Remove gradient");
        gradientRemover.removeGradient(inputImage, null, outputImage);
      }

      System.out.println("Save " + outputFile);
      ImageWriter.write(outputImage, outputFile);

      System.out.println("Finished " + inputFile + " -> " + outputFile);
    }
  }

  private static void autoSetFixPoints(GradientRemover gradientRemover, DoubleImage image) {
    int sampleWidth = image.getWidth() / 5;
    int sampleHeight = image.getHeight() / 5;
    int sampleRadius = Math.min(sampleWidth, sampleHeight) / 2;

    int x1 = image.getWidth() / 2;
    int y1 = sampleHeight / 2;
    double[] color1 = ImageUtil.averagePixel(
        image,
        x1,
        y1,
        sampleRadius,
        ColorModel.RGB,
        null);

    int x2 = sampleWidth / 2;
    int y2 = image.getHeight() - sampleHeight / 2;
    double[] color2 = ImageUtil.averagePixel(
        image,
        x2,
        y2,
        sampleRadius,
        ColorModel.RGB,
        null);

    int x3 = image.getWidth() - sampleWidth / 2;
    int y3 = image.getHeight() - sampleHeight / 2;
    double[] color3 = ImageUtil.averagePixel(
        image,
        x3,
        y3,
        sampleRadius,
        ColorModel.RGB,
        null);

    gradientRemover.setFixPoints(
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
}
