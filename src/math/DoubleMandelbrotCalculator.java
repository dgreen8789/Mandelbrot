package math;

import graphics.colors.Histogram;
import java.awt.Point;

/**
 *
 * @author David
 */
public class DoubleMandelbrotCalculator {

    public static int MAX_ITERATIONS = 8192;
    private static final int ZOOM_WAIT_TIME = 50;
    private static final int PAN_WAIT_TIME = 5;
    private static double[] xCoords;
    private static double[] yCoords;
    private static double xEpsilon;
    private static double yEpsilon;

    private static Histogram histogram;
    private static int[][] data;
    static CalculatorThread[] threads;

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
    public static void draw(DoubleWindow window) {
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
        }
        runCalculations(threads, ZOOM_WAIT_TIME);
        long stop = System.currentTimeMillis();
        System.out.println("Calculation took " + (stop - start) + " ms");
        //System.out.println("\n\n\n");
    }

    public static double[] coordinateByPoint(Point p, DoubleWindow window) {
        return new double[]{
            window.xCenter - window.xRange + p.x * xEpsilon,
            window.yCenter - window.yRange + p.y * yEpsilon
        };
    }

    public static Histogram getHistogram() {
        return histogram;
    }

    public static void panRight(int distance, DoubleWindow window) {
        long start = System.currentTimeMillis();
        int i;
        for (i = 0; i < xCoords.length - distance; i++) {
            xCoords[i] = xCoords[i + distance];
            data[i] = data[i + distance];
        }
        for (; i < data.length; i++) {
            for (int dp : data[i]) {
                histogram.decrement(dp);
            }
            data[i] = new int[data[0].length];
            xCoords[i] = xCoords[i - 1] + xEpsilon;
        }
        for (i = 0; i < threads.length; i++) {
            int[] miniWindow = new int[]{
                xCoords.length - distance,
                xCoords.length,
                i * yCoords.length / threads.length,
                (i + 1) * yCoords.length / threads.length
            };
            threads[i].setMiniWindow(miniWindow);
        }
        runCalculations(threads, PAN_WAIT_TIME);
        window.shiftRight(distance * xEpsilon);
        long stop = System.currentTimeMillis();
        System.out.println("Panning right " + distance + " pixels took " + (stop - start) + " ms");

    }

    public static void panLeft(int distance, DoubleWindow window) {
        long start = System.currentTimeMillis();
        int i;
        for (i = xCoords.length - distance - 1; i >= 0; i--) {
            xCoords[i + distance] = xCoords[i];
            data[i + distance] = data[i];
        }
        for (i = distance - 1; i >= 0; i--) {
            for (int dp : data[i]) {
                histogram.decrement(dp);
            }
            data[i] = new int[data[0].length];
            xCoords[i] = xCoords[i + 1] - xEpsilon;
        }
        for (i = 0; i < threads.length; i++) {
            int[] miniWindow = new int[]{
                0,
                distance,
                i * yCoords.length / threads.length,
                (i + 1) * yCoords.length / threads.length
            };
            //System.out.println(Arrays.toString(miniWindow));
            threads[i].setMiniWindow(miniWindow);
        }
        runCalculations(threads, PAN_WAIT_TIME);
        window.shiftLeft(distance * xEpsilon);
        long stop = System.currentTimeMillis();
        System.out.println("Panning left " + distance + " pixels took " + (stop - start) + " ms");

    }

    public static void panDown(int distance, DoubleWindow window) {
        long start = System.currentTimeMillis();
        for (int[] row : data) {
            for (int i = 0; i < distance; i++) {
                histogram.decrement(row[i]);
            }
            System.arraycopy(row, distance, row, 0, row.length - distance);
        }
        System.arraycopy(yCoords, distance, yCoords, 0, yCoords.length - distance);
        int i;
        for (i = yCoords.length - distance; i < yCoords.length; i++) {
            yCoords[i] = yCoords[i - 1] + yEpsilon;
        }

        for (i = 0; i < threads.length; i++) {
            int[] miniWindow = new int[]{
                i * xCoords.length / threads.length,
                (i + 1) * xCoords.length / threads.length,
                yCoords.length - distance,
                yCoords.length
            };
            threads[i].setMiniWindow(miniWindow);
        }
        runCalculations(threads, PAN_WAIT_TIME);
        window.shiftDown(distance * yEpsilon);
        long stop = System.currentTimeMillis();
        System.out.println("Panning down " + distance + " pixels took " + (stop - start) + " ms");

    }

    public static void panUp(int distance, DoubleWindow window) {
        long start = System.currentTimeMillis();
        for (int[] row : data) {
            for (int i = row.length - distance; i < row.length; i++) {
                histogram.decrement(row[i]);
            }
            System.arraycopy(row, 0, row, distance, row.length - distance);
        }
        System.arraycopy(yCoords, 0, yCoords, distance, yCoords.length - distance);
        int i;
        for (i = distance - 1; i >= 0; i--) {
            yCoords[i] = yCoords[i + 1] - yEpsilon;
        }

        for (i = 0; i < threads.length; i++) {
            int[] miniWindow = new int[]{
                i * xCoords.length / threads.length,
                (i + 1) * xCoords.length / threads.length,
                0,
                distance
            };
            threads[i].setMiniWindow(miniWindow);
        }
        runCalculations(threads, PAN_WAIT_TIME);
        window.shiftUp(distance * yEpsilon);
        long stop = System.currentTimeMillis();
        System.out.println("Panning up " + distance + " pixels took " + (stop - start) + " ms");
    }

    private static boolean calculating(CalculatorThread[] threads) {
        boolean done_calculating = true;
        for (CalculatorThread thread : threads) {
            done_calculating &= (thread.getState().equals(Thread.State.TIMED_WAITING));
        }
        return !done_calculating;
    }

    private static void runCalculations(CalculatorThread[] threads, long waitTime) {
        for (CalculatorThread thread : threads) {
            thread.interrupt();
        }
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
        }
        while (calculating(threads));
    }

}
