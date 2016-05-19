package math;

import graphics.colors.Histogram;
import java.awt.Point;
import static java.lang.Double.doubleToRawLongBits;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.DoubleConsts;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author David
 */
public class DoubleMandelbrotCalculator {

    public static int MAX_ITERATIONS = 8192;
    private static double[] xCoords;
    private static double[] yCoords;
    private static double xEpsilon;
    private static double yEpsilon;
   
    private static Histogram histogram;
    private static int[][] data;
    static  CalculatorThread[] threads;

    public static void initialize(int numThreads, int width, int height, int[][] data) {
        histogram = new Histogram(MAX_ITERATIONS);
        threads = new CalculatorThread[numThreads];
        xCoords = new double[width];
        yCoords = new double[height];
        DoubleMandelbrotCalculator.data = data;
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new CalculatorThread(histogram, xCoords, yCoords, data);
            threads[i].start();
            threads[i].setName("Drawer thread " + i);
            System.out.println("Thread: " + threads[i].getName() + " started");
  
        }

        System.out.println("");
    }

    /**
     * Draws the Mandelbrot set
     *
     * @param window
     * @param data
     * @return
     */
    public static boolean draw(DoubleWindow window) {
        long start = System.currentTimeMillis();
        System.out.println(window);
        xEpsilon = window.xRange / data.length * 2;
        yEpsilon = window.yRange / data[0].length * 2;
        xCoords[0] = window.xCenter - window.xRange;
        for (int i = 1; i < xCoords.length; i++) {
            xCoords[i] = xCoords[i - 1] + xEpsilon;
        }
        yCoords[0] = window.yCenter - window.yRange;
        for (int i = 1; i < yCoords.length; i++) {
            yCoords[i] = yCoords[i - 1] + yEpsilon;
        }
        //System.out.println(xDelta);
        for (int i = 0; i < threads.length; i++) {
            int[] miniWindow = new int[]{
                i * xCoords.length / threads.length,
                (i + 1) * xCoords.length / threads.length,
                0,
                yCoords.length
            };
            threads[i].setMiniWindow(miniWindow);
            threads[i].interrupt();
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
        }
        do {
            done_calculating = true;
            for (CalculatorThread thread : threads) {
                done_calculating &= (thread.getState().equals(Thread.State.TIMED_WAITING));
            }
        } while (!done_calculating);
        long stop = System.currentTimeMillis();
        System.out.println("Calculation took " + (stop - start) + " ms");
        //System.out.println("\n\n\n");
        return true;
    }
    private static boolean done_calculating;

    public static double[] coordinateByPoint(Point p, DoubleWindow window) {
        return new double[]{
            window.xCenter - window.xRange + p.x * xEpsilon,
            window.yCenter - window.yRange + p.y * yEpsilon
        };
    }

    public static Histogram getHistogram() {
        return histogram;
    }

}
