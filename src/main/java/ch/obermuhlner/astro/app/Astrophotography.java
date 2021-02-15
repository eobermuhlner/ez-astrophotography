package ch.obermuhlner.astro.app;

import ch.obermuhlner.astro.app.CommandParser.Command;
import ch.obermuhlner.astro.app.CommandParser.Option;
import ch.obermuhlner.astro.gradient.GradientRemover;
import ch.obermuhlner.astro.gradient.Point;
import ch.obermuhlner.astro.gradient.correction.LinearSampleSubtraction;
import ch.obermuhlner.astro.gradient.correction.SimpleSampleSubtraction;
import ch.obermuhlner.astro.gradient.correction.SplineSampleSubtraction;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageCreator;
import ch.obermuhlner.astro.image.ImageQuality;
import ch.obermuhlner.astro.image.ImageReader;
import ch.obermuhlner.astro.image.ImageWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Astrophotography {

  public static void main(String[] args) throws IOException {
    GradientRemover gradientRemover = new GradientRemover();

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
        "gradient", "--point", "100,100", "--point", "-100,-100",
//        "curve-linear",
        "curve-spline", "--gradient-factor", "0.02", "--stretch", "0.6", "0.9",
        "output", "images/Test.tif"

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

      System.out.println("Processing " + inputFile);

      DoubleImage inputImage = ImageReader.read(inputFile, ImageQuality.High);
      DoubleImage outputImage = ImageCreator.create(inputImage.getWidth(), inputImage.getHeight(), ImageQuality.High);

      gradientRemover.setFixPoints(correctFixPoints(gradientPoints, inputImage), inputImage, sampleRadius.get());

      gradientRemover.removeGradient(inputImage, null, outputImage);

      ImageWriter.write(outputImage, outputFile);
    }
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
