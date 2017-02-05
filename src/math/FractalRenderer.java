/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import architecture.Window;
import graphics.colors.ColorScheme;
import graphics.colors.Histogram;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import math.numbertypes.DoubleNT;
import math.numbertypes.NumberType;

/**
 *
 * @author David
 */
public abstract class FractalRenderer {

    protected NumberType[] xCoords;
    protected NumberType[] yCoords;
    protected BufferedImage image;
    protected Window window;
    protected NumberType xEpsilon;
    protected NumberType yEpsilon;
    protected Histogram histogram;
    protected int[][] data;
    protected boolean[][] valid;

    protected static final Class DEFAULT_CLASS = DoubleNT.class;

    public FractalRenderer(int width, int height) {
        this(width, height, DEFAULT_CLASS);
    }

    public FractalRenderer(int width, int height, Class theClass) {
        xCoords = (NumberType[]) Array.newInstance(theClass, width);
        yCoords = (NumberType[]) Array.newInstance(theClass, height);
        data = new int[width][height];
        valid = new boolean[width][height];
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        resetWindow();
    }

    public NumberType[] coordinateByPoint(Point p) {
        //System.out.println("Clicked " + p);
        return new NumberType[]{
            window.xCenter.subtract(window.xRange).add(xEpsilon.multiply(p.x)),
            window.yCenter.add(window.yRange).subtract(yEpsilon.multiply(p.y))
        };
    }

    protected Window changeWindow(boolean up, Window window) {
        double zl = window.getZoomLevel();
        window = new Window(
                up ? window.xCenter.toNextSystem() : window.xCenter.toPreviousSystem(),
                up ? window.yCenter.toNextSystem() : window.yCenter.toPreviousSystem(),
                up ? window.xRange.toNextSystem() : window.xRange.toPreviousSystem(),
                up ? window.yRange.toNextSystem() : window.yRange.toPreviousSystem()
        );
        window.setZoomLevel(zl);
        return window;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public Window getWindow() {
        //System.out.println("called to:\t  " + window);
        return window;
    }

    public int[][] getDataArray() {
        return data;
    }

    public abstract void changeNumberSystem(boolean up);

    public abstract int getMaxIterations();

    public abstract void zoom(boolean deeper, Point p, double factor);

    public abstract void panUp(int distance);

    public abstract void panDown(int distance);

    public abstract void panLeft(int distance);

    public abstract void panRight(int distance);

    public abstract void draw();

    protected abstract void beginRender(boolean killThreads, int x0, int y0, int x1, int y1) ;

    public abstract boolean hasBoxes();

    public abstract ArrayList<MRectangle> getBoxes();

    public BufferedImage createImage(boolean newImg, ColorScheme scheme, double theta) {
        //System.out.println("Histogram " + histogram.toString());
        TreeMap<Integer, Integer> colors = ColorScheme.generate(histogram, scheme);
        if (colors == null) {
            //System.out.println("COLORING FAIL");
            return image;
        }
        double sinTheta = Math.sin(theta);
        image = newImg ? new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_ARGB) : image;
        for (int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[0].length; y++) {
                if (valid[x][y]) {
                    Integer color = colors.get(data[x][y]);
                    //colors.replace(colors.lastKey(), Color.RED.getRGB()); 
                    //makes the most expensive renders be red
                    if (color == null) {
                        image.setRGB(x, (int) (y * sinTheta), Color.BLACK.getRGB());
                        // System.out.printf("Color not found. Value of (%d, %d) is %d\n", x, y, data[x][y]);//comment block for box tracing
                        //System.out.println("histogram has count " + histogram.get(data[x][y]));
                    } else {
                        image.setRGB(x, (int) (y * sinTheta), color);

                    }
                }
            }
        }
        return image;
    }

    public double propPixelsRendered() {
        //System.out.println(histogram.getValidPixelCounter().get());
        //System.out.println( data.length * data[0].length);
        return histogram.getValidPixelCounter().get() / ((double) data.length * data[0].length);
    }
    public int numPixelsRendered(){
        return histogram.getValidPixelCounter().get();
    }
    public void resetData() {
        for (boolean[] d : valid) {
            Arrays.fill(d, false);

        }
        histogram.getValidPixelCounter().set(0);
    }

    public abstract void resetWindow();
    
    public abstract void resize(int newX, int newY);
}
