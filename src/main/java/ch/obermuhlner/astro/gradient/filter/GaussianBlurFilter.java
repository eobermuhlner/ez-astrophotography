package ch.obermuhlner.astro.gradient.filter;

import ch.obermuhlner.astro.image.ArrayDoubleImage;
import ch.obermuhlner.astro.image.ColorModel;
import ch.obermuhlner.astro.image.DoubleImage;
import ch.obermuhlner.astro.image.ImageUtil;

import java.io.File;
import java.io.IOException;

// http://blog.ivank.net/fastest-gaussian-blur.html
public class GaussianBlurFilter implements Filter {

  private final int radius;
  private final ColorModel model;

  public GaussianBlurFilter(int radius, ColorModel model) {
    this.radius = radius;
    this.model = model;
  }

  @Override
  public void filter(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height) {
    //simpleFilter(source, sourceX, sourceY, target, targetX, targetY, width, height);
    optimizedFilter(source, sourceX, sourceY, target, targetX, targetY, width, height);
  }

  private void simpleFilter(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height) {
    int kernelRadius = (int) (Math.ceil(radius * 2.57) + 0.5);
    double[] samples = new double[3];
    for (int dy = 0; dy < height; dy++) {
      for (int dx = 0; dx < width; dx++) {
        double sum0 = 0;
        double sum1 = 0;
        double sum2 = 0;
        double weightSum = 0;
        for (int kx = dx-kernelRadius; kx <= dx+kernelRadius; kx++) {
          for (int ky = dy-kernelRadius; ky <= dy+kernelRadius; ky++) {
            int sx = Math.min(sourceX+width - 1, Math.max(sourceX, sourceX + kx));
            int sy = Math.min(sourceY+height - 1, Math.max(sourceY, sourceY + ky));
            double dsq = (kx-dx)*(kx-dx)+(ky-dy)*(ky-dy);
            double weight = Math.exp( -dsq / (2*radius*radius) ) / (Math.PI*2*radius*radius);
            source.getPixel(sx, sy, model, samples);
            sum0 += samples[0] * weight;
            sum1 += samples[1] * weight;
            sum2 += samples[2] * weight;
            weightSum += weight;
          }
          int tx = targetX + dx;
          int ty = targetY + dy;
          if (ImageUtil.isInsideImage(target, tx, ty)) {
            samples[0] = sum0 / weightSum;
            samples[1] = sum1 / weightSum;
            samples[2] = sum2 / weightSum;
            target.setPixel(tx, ty, model, samples);
          }
        }
      }
    }
  }

  private static class SwapImage {

    DoubleImage source;
    DoubleImage target;

    SwapImage(DoubleImage source, DoubleImage target) {
      this.source = source;
      this.target = target;
    }

    void swap() {
      DoubleImage tmp = source;
      source = target;
      target = tmp;
    }
  }

  private void optimizedFilter(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height) {
    SwapImage temp = new SwapImage(
        new ArrayDoubleImage(width, height, model),
        new ArrayDoubleImage(width, height, model)
    );

    ImageUtil.copyPixels(source, sourceX, sourceY, temp.source, 0, 0, width, height, model);

    double[] boxSizes = boxSizesForGauss(radius, 3);
    for (double boxSize : boxSizes) {
      int boxRadius = (int) (Math.ceil((boxSize-1)/2)  + 0.5);
      boxBlur(temp.source, 0, 0, temp.target, 0, 0, width, height, boxRadius);
      temp.swap();
    }

    ImageUtil.copyPixels(temp.source, 0, 0, target, targetX, targetY, width, height, model);
  }

  public void boxBlur(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height, int boxRadius) {
    ArrayDoubleImage tempImage = new ArrayDoubleImage(width, height, model);

    boxBlurHorizontal(source, sourceX, sourceY, tempImage, 0, 0, width, height, boxRadius);
    boxBlurVertical(tempImage, 0, 0, target, targetX, targetY, width, height, boxRadius);
  }

  private void boxBlurHorizontal(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height, int boxRadius) {
    int kernelSize = boxRadius + boxRadius + 1;
    double[] samples = new double[3];
    for (int dy = 0; dy < height; dy++) {
      for (int dx = 0; dx < width; dx++) {
        double sum0 = 0;
        double sum1 = 0;
        double sum2 = 0;
        int sy = Math.min(sourceY + height - 1, sourceY + dy);
        for (int kx = dx-boxRadius; kx <= dx+boxRadius; kx++) {
          int sx = Math.min(sourceX + width - 1, Math.max(sourceX, sourceX + kx));
          source.getPixel(sx, sy, model, samples);
          sum0 += samples[0];
          sum1 += samples[1];
          sum2 += samples[2];
        }
        int tx = targetX + dx;
        int ty = targetY + dy;
        if (ImageUtil.isInsideImage(target, tx, ty)) {
          samples[0] = sum0 / kernelSize;
          samples[1] = sum1 / kernelSize;
          samples[2] = sum2 / kernelSize;
          target.setPixel(tx, ty, model, samples);
        }
      }
    }
  }

  private void boxBlurVertical(DoubleImage source, int sourceX, int sourceY, DoubleImage target, int targetX, int targetY, int width, int height, int boxRadius) {
    int kernelSize = boxRadius + boxRadius + 1;
    double[] samples = new double[3];
    for (int dy = 0; dy < height; dy++) {
      for (int dx = 0; dx < width; dx++) {
        double sum0 = 0;
        double sum1 = 0;
        double sum2 = 0;
        int sx = Math.min(sourceX + width - 1, sourceX + dx);
        for (int ky = dy-boxRadius; ky <= dy+boxRadius; ky++) {
          int sy = Math.min(sourceY + height - 1, Math.max(sourceY, sourceY + ky));
          source.getPixel(sx, sy, model, samples);
          sum0 += samples[0];
          sum1 += samples[1];
          sum2 += samples[2];
        }
        int tx = targetX + dx;
        int ty = targetY + dy;
        if (ImageUtil.isInsideImage(target, tx, ty)) {
          samples[0] = sum0 / kernelSize;
          samples[1] = sum1 / kernelSize;
          samples[2] = sum2 / kernelSize;
          target.setPixel(tx, ty, model, samples);
        }
      }
    }
  }

  private double[] boxSizesForGauss(double sigma, int n) {
    var wIdeal = Math.sqrt((12*sigma*sigma/n)+1);  // Ideal averaging filter width
    var wl = Math.floor(wIdeal);  if(wl%2==0) wl--;
    var wu = wl+2;

    var mIdeal = (12*sigma*sigma - n*wl*wl - 4*n*wl - 3*n)/(-4*wl - 4);
    var m = Math.round(mIdeal);
    // var sigmaActual = Math.sqrt( (m*wl*wl + (n-m)*wu*wu - n)/12 );

    var sizes = new double [n];
    for(var i=0; i<n; i++) {
      sizes[i] = i<m?wl:wu;
    }
    return sizes;
  }

  @Override
  public String toString() {
    return "GaussianBlur(radius=" + radius + ")";
  }
}
