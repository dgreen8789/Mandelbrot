package math;

import graphics.colors.Histogram;
import java.awt.Point;
import java.lang.reflect.Array;

/**
 *
 * @author David
 */
public class MandelbrotCalculator {

    public static int MAX_ITERATIONS = 4096;

    private static final int ZOOM_WAIT_TIME = 50;
    private static final int PAN_WAIT_TIME = 5;
    private static NumberType[] xCoords;
    private static NumberType[] yCoords;
    private static NumberType xEpsilon;
    private static NumberType yEpsilon;

    private static NumberType ZERO;
    private static NumberType ONE;
    private static NumberType TEN;
    private static int maxZoom;
    private static Histogram histogram;
    private static int[][] data;
    static CalculatorThread[] threads;

    public static final Class[] NUMBER_SYSTEMS = new Class[]{DoubleNumberType.class, DoubleDoubleNumberType.class, QuadDoubleNumberType.class};
    private static int currentSystem;

    private static Window changeNumberSystem(Class numberType, Window window) {
        changeNumberSystem(numberType);
        int zl = window.getZoomLevel();
        window = new Window(
                window.xCenter.toNextSystem(),
                window.yCenter.toNextSystem(),
                window.xRange.toNextSystem(),
                window.yRange.toNextSystem()
        );
        window.setZoomLevel(zl);
        return window;
    }

    public static void changeNumberSystem(Class numberType) {
        xCoords = (NumberType[]) Array.newInstance(numberType, xCoords.length);
        yCoords = (NumberType[]) Array.newInstance(numberType, yCoords.length);
        for (CalculatorThread thread : threads) {
            if (thread != null) {
                thread.setxCoords(xCoords);
                thread.setyCoords(yCoords);
            }
        }
        try {
            ZERO = (NumberType) numberType.getDeclaredField("ZERO").get(null);
            ONE = (NumberType) numberType.getDeclaredField("ONE").get(null);
            TEN = (NumberType) numberType.getDeclaredField("TEN").get(null);
            maxZoom = numberType.getDeclaredField("MAX_ZOOM").getInt(null);
        } catch (NoSuchFieldException ex) {
            System.out.println("Numerical fields not found for numbertype: " + numberType);
        } catch (SecurityException ex) {
            System.out.println("Rando security exception");
        } catch (IllegalArgumentException ex) {
            System.out.println("Rando illegal arg exception");
        } catch (IllegalAccessException ex) {
            System.out.println("Rando illegal access exception");
        }
        System.out.println("Switched to number system " + numberType.getSimpleName());
    }

    public static void initialize(int numThreads, int width, int height, int[][] data, int numberType) {
        xCoords = (NumberType[]) Array.newInstance(NumberType.class, width);
        yCoords = (NumberType[]) Array.newInstance(NumberType.class, height);
        histogram = new Histogram(MAX_ITERATIONS);
        threads = new CalculatorThread[numThreads];
        currentSystem = numberType;
        changeNumberSystem(NUMBER_SYSTEMS[currentSystem]);

        MandelbrotCalculator.data = data;
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new CalculatorThread(histogram, xCoords, yCoords, data);
            threads[i].start();
            threads[i].setName("Drawer thread " + i);
            System.out.println("Thread: " + threads[i].getName() + " started");

        }

    }

    /**
     * Draws the Mandelbrot set
     *
     * @param window
     * @param data
     * @return
     */
    public static Window draw(Window window) {
        long start = System.currentTimeMillis();
        if (window.getZoomLevel() > maxZoom ) {
            System.out.println("Precision fail imminent, ");
            if (currentSystem < NUMBER_SYSTEMS.length - 1) {
                currentSystem = (currentSystem == NUMBER_SYSTEMS.length - 1 ? currentSystem : currentSystem + 1);
                System.out.println("Attempting number system #" + currentSystem);

                window = changeNumberSystem(NUMBER_SYSTEMS[currentSystem], window);
            }
        }
        xEpsilon = window.xRange.divide(data.length).mult2();
        yEpsilon = window.yRange.divide(data[0].length).mult2();
        xCoords[0] = window.xCenter.subtract(window.xRange);
        for (int i = 1; i < xCoords.length; i++) {
            xCoords[i] = xCoords[i - 1].add(xEpsilon);
        }
        yCoords[0] = window.yCenter.subtract(window.yRange);
        for (int i = 1; i < yCoords.length; i++) {
            yCoords[i] = yCoords[i - 1].add(yEpsilon);
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
        return window;
        //System.out.println("\n\n\n");
    }

    public static NumberType[] coordinateByPoint(Point p, Window window) {
        System.out.println("Coord by point vars");
        System.out.println(window);
        System.out.println(xEpsilon);
        System.out.println(yEpsilon);
        return new NumberType[]{
            window.xCenter.subtract(window.xRange).add(xEpsilon.multiply(p.x)),
            window.yCenter.subtract(window.yRange).add(yEpsilon.multiply(p.y))
        };
    }

    public static Histogram getHistogram() {
        return histogram;
    }

    public static void panRight(int distance, Window window) {
        long start = System.currentTimeMillis();
        int i;
        for (i = 0; i < xCoords.length - distance; i++) {
            xCoords[i] = xCoords[i + distance];
            data[i] = data[i + distance];
        }
        for (; i < data.length; i++) {
            data[i] = new int[data[0].length];
            xCoords[i] = xCoords[i - 1].add(xEpsilon);
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
        window.shiftRight(xEpsilon.multiply(distance));
        long stop = System.currentTimeMillis();
        System.out.println("Panning right " + distance + " pixels took " + (stop - start) + " ms");

    }

    public static void panLeft(int distance, Window window) {
        long start = System.currentTimeMillis();
        int i;
        for (i = xCoords.length - distance - 1; i >= 0; i--) {
            xCoords[i + distance] = xCoords[i];
            data[i + distance] = data[i];
        }
        for (i = distance - 1; i >= 0; i--) {
            data[i] = new int[data[0].length];
            xCoords[i] = xCoords[i + 1].subtract(xEpsilon);
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
        window.shiftLeft(xEpsilon.multiply(distance));
        long stop = System.currentTimeMillis();
        System.out.println("Panning left " + distance + " pixels took " + (stop - start) + " ms");

    }

    public static void panDown(int distance, Window window) {
        long start = System.currentTimeMillis();
        for (int[] row : data) {
            System.arraycopy(row, distance, row, 0, row.length - distance);
        }
        System.arraycopy(yCoords, distance, yCoords, 0, yCoords.length - distance);
        int i;
        for (i = yCoords.length - distance; i < yCoords.length; i++) {
            yCoords[i] = yCoords[i - 1].add(yEpsilon);
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
        window.shiftDown(yEpsilon.multiply(distance));
        long stop = System.currentTimeMillis();
        System.out.println("Panning down " + distance + " pixels took " + (stop - start) + " ms");

    }

    public static void panUp(int distance, Window window) {
        long start = System.currentTimeMillis();
        for (int[] row : data) {
            System.arraycopy(row, 0, row, distance, row.length - distance);
        }
        System.arraycopy(yCoords, 0, yCoords, distance, yCoords.length - distance);
        int i;
        for (i = distance - 1; i >= 0; i--) {
            yCoords[i] = yCoords[i + 1].subtract(yEpsilon);
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
        window.shiftUp(yEpsilon.multiply(distance));
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
