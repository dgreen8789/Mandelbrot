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
    static CalculatorThread[] threads;

    public static void initialize(int numThreads, int width, int height) {
        histogram = new Histogram(MAX_ITERATIONS);
        threads = new CalculatorThread[numThreads];
        xCoords = new double[width];
        yCoords = new double[height];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new CalculatorThread(histogram);
            threads[i].start();
            threads[i].setName("Drawer thread " + i);
            System.out.println("Thread: " + threads[i].getName() + " started");
            threads[i].setxCoords(xCoords);
            threads[i].setyCoords(yCoords);
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
    public static boolean draw(int[][] data, DoubleWindow window) {
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
            //System.out.println(Arrays.toString(miniWindow));
            threads[i].setMiniWindow(miniWindow);
            threads[i].setBuffer(data);
            threads[i].signalRecalculate();
            threads[i].interrupt();
        }

        do {
            done_calculating = true;
            for (CalculatorThread thread : threads) {
                done_calculating &= !thread.doneCalculating();
            }
        } while (!done_calculating);
        long stop = System.currentTimeMillis();
        System.out.println((stop - start) / 1000.0 + " sec");
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
