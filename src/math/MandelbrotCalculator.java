package math;

import architecture.Pool;
import graphics.base.GraphicsController;
import math.numbertypes.DoubleNT;
import math.numbertypes.DoubleDouble;
import graphics.colors.Histogram;
import java.awt.Point;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import static math.BoxedEscape.NOT_CALCULATED_CONST;

/**
 *
 * @author David
 */
public class MandelbrotCalculator {

    public static int MAX_ITERATIONS = 8192;
    public static int SUPER_SAMPLING_FACTOR = 1; //sqrt of super sampling

    private static final int ZOOM_WAIT_TIME = 50;
    private static final int PAN_WAIT_TIME = 5;
    private static NumberType[] xCoords;
    private static NumberType[] yCoords;
    private static NumberType xEpsilon;
    private static NumberType yEpsilon;
    private static NumberType ssxEpsilon;
    private static NumberType ssyEpsilon;

    private volatile Window window;
    private final int[][] data;
    private static NumberType ZERO;
    private static NumberType ONE;
    private static NumberType TEN;
    private static int maxZoom;
    private static Histogram histogram;
    private static Pool<MRectangle> inPool;
    private static Pool<MRectangle> outPool;

    private static BoxedEscape[] threads;
    private static SuperSampleEngine sampler;

    public static final Class[] NUMBER_SYSTEMS = new Class[]{DoubleNT.class, DoubleDouble.class};
    private static int currentSystem;

    public MandelbrotCalculator(int width, int height, int numberType) {
        window
                = //DoubleWindow.fromString(str);
                new Window(new DoubleNT(-.75),
                        new DoubleNT(0),
                        new DoubleNT(1.75),
                        new DoubleNT(1));
        xCoords = (NumberType[]) Array.newInstance(NumberType.class, width);
        yCoords = (NumberType[]) Array.newInstance(NumberType.class, height);
        data = new int[width][height];
        histogram = new Histogram(MAX_ITERATIONS);
        threads = new BoxedEscape[GraphicsController.THREAD_COUNT];
        currentSystem = numberType;
        sampler = new SuperSampleEngine(histogram);
        changeNumberSystem(NUMBER_SYSTEMS[currentSystem]);
        inPool = new Pool<>();
        outPool = new Pool<>();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new BoxedEscape(xCoords, yCoords, data);
            threads[i].start();
            threads[i].setName("Drawer thread " + i);
            System.out.println("Thread: " + threads[i].getName() + " started");

        }

    }

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
        for (BoxedEscape thread : threads) {
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

    public void zoom(boolean deeper, Point p) {
        NumberType[] coords = coordinateByPoint(p);
        if (deeper) {
            window.zoomIn(coords[0], coords[1]);
        } else {
            window.zoomOut(coords[0], coords[1]);
        }
        draw();
    }

    public void draw() {

        System.out.println(window);
        long start = System.currentTimeMillis();
        if (window.getZoomLevel() > maxZoom) {
            System.out.println("Precision fail imminent, ");
            if (currentSystem < NUMBER_SYSTEMS.length - 1) {
                currentSystem = currentSystem + 1;
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
        for (int[] d : data) {
            Arrays.fill(d, NOT_CALCULATED_CONST);
        }
        for (int i = 0; i < threads.length; i++) {
            inPool.add(new MRectangle(
                    i * xCoords.length / threads.length,
                    0,
                    xCoords.length / threads.length,
                    yCoords.length));
        }
        runCalculations(threads, ZOOM_WAIT_TIME);
        long stop = System.currentTimeMillis();
        System.out.println("Calculation took " + (stop - start) + " ms");
        //System.out.println("\n\n\n");
    }

    public NumberType[] coordinateByPoint(Point p) {
        System.out.println(p);
        System.out.println("Coord by point vars");
        System.out.println(window);
        System.out.println(xEpsilon);
        System.out.println(yEpsilon);
        return new NumberType[]{
            window.xCenter.subtract(window.xRange).add(xEpsilon.multiply(p.x)),
            window.yCenter.subtract(window.yRange).add(yEpsilon.multiply(p.y))
        };
    }

    public synchronized Histogram getHistogram() {
        return histogram;
    }

    public void panRight(int distance) {
        long start = System.currentTimeMillis();
        int i;
        for (i = 0; i < xCoords.length - distance; i++) {
            xCoords[i] = xCoords[i + distance];
            data[i] = data[i + distance];
        }
        for (; i < data.length; i++) {
            data[i] = new int[data[0].length];
            Arrays.fill(data[i], NOT_CALCULATED_CONST);
            xCoords[i] = xCoords[i - 1].add(xEpsilon);
        }
        for (i = 0; i < threads.length; i++) {
            inPool.add(new MRectangle(
                    xCoords.length - distance,
                     i * yCoords.length / threads.length,
                    distance,                  
                    yCoords.length / threads.length
            ));
        }
        runCalculations(threads, PAN_WAIT_TIME);
        window.shiftRight(xEpsilon.multiply(distance));
        long stop = System.currentTimeMillis();
        System.out.println("Panning right " + distance + " pixels took " + (stop - start) + " ms");

    }

    public void panLeft(int distance) {
        long start = System.currentTimeMillis();
        int i;
        for (i = xCoords.length - distance - 1; i >= 0; i--) {
            xCoords[i + distance] = xCoords[i];
            data[i + distance] = data[i];
        }
        for (i = distance - 1; i >= 0; i--) {
            data[i] = new int[data[0].length];
            Arrays.fill(data[i], NOT_CALCULATED_CONST);
            xCoords[i] = xCoords[i + 1].subtract(xEpsilon);
        }
        for (i = 0; i < threads.length; i++) {
            inPool.add(new MRectangle(
                    0,
                    i * yCoords.length / threads.length,
                    distance,
                    yCoords.length / threads.length
            ));
            //System.out.println(Arrays.toString(miniWindow));
        }
        runCalculations(threads, PAN_WAIT_TIME);
        window.shiftLeft(xEpsilon.multiply(distance));
        long stop = System.currentTimeMillis();
        System.out.println("Panning left " + distance + " pixels took " + (stop - start) + " ms");

    }

    public void panDown(int distance) {
        long start = System.currentTimeMillis();
        for (int[] row : data) {
            System.arraycopy(row, distance, row, 0, row.length - distance);
            Arrays.fill(row, row.length - distance, row.length, NOT_CALCULATED_CONST);

        }
        System.arraycopy(yCoords, distance, yCoords, 0, yCoords.length - distance);
        int i;
        for (i = yCoords.length - distance; i < yCoords.length; i++) {
            yCoords[i] = yCoords[i - 1].add(yEpsilon);
        }

        for (i = 0; i < threads.length; i++) {
            inPool.add(new MRectangle(
                    i * xCoords.length / threads.length,
                 yCoords.length - distance
                   ,xCoords.length / threads.length,
                    distance
            ));
        }
        runCalculations(threads, PAN_WAIT_TIME);
        window.shiftDown(yEpsilon.multiply(distance));
        long stop = System.currentTimeMillis();
        System.out.println("Panning down " + distance + " pixels took " + (stop - start) + " ms");

    }

    public void panUp(int distance) {
        System.out.println(distance);
        long start = System.currentTimeMillis();
        for (int[] row : data) {
            System.arraycopy(row, 0, row, distance, row.length - distance);
            Arrays.fill(row, 0, distance, NOT_CALCULATED_CONST);
        }

        System.arraycopy(yCoords, 0, yCoords, distance, yCoords.length - distance);
        int i;
        for (i = distance - 1; i >= 0; i--) {
            yCoords[i] = yCoords[i + 1].subtract(yEpsilon);
        }

        for (i = 0; i < threads.length; i++) {
            inPool.add(new MRectangle(
                    i * xCoords.length / threads.length,
                    0,
                    xCoords.length / threads.length,
                    distance
            ));
        }
        System.out.println(inPool.getValues());
        runCalculations(threads, PAN_WAIT_TIME);
        window.shiftUp(yEpsilon.multiply(distance));
        long stop = System.currentTimeMillis();
        System.out.println("Panning up " + distance + " pixels took " + (stop - start) + " ms");
    }

    private static void runCalculations(BoxedEscape[] threads, long waitTime) {
        outPool = new Pool<>();
        for (BoxedEscape thread : threads) {
            thread.setInPool(inPool);
            thread.setOutPool(outPool);
            thread.interrupt();
        }
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
        }
        while (!inPool.isEmpty());
        System.out.println(outPool.size() + " boxes");
    }

    public ArrayList<MRectangle> getBoxes() {
        return outPool.getValues();
    }

    public synchronized int[][] getDataArray() {

        for (int[] data1 : data) {
            for (int d : data1) {
                histogram.increment(d);
            }
        }
        return data;
    }

    public Window getWindow() {
        return window;
    }

    public void superSample() {
        ssxEpsilon = xEpsilon.divide(SUPER_SAMPLING_FACTOR);
        ssyEpsilon = xEpsilon.divide(SUPER_SAMPLING_FACTOR);
    }

}
