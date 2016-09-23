package math;

import architecture.Window;
import math.numbertypes.NumberType;
import architecture.Pool;
import graphics.base.GraphicsController;
import math.numbertypes.DoubleNT;
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
public class MandelbrotRenderer extends FractalRenderer {

    public static int MAX_ITERATIONS = 8192;

    protected int maxZoom;
    protected Pool<MRectangle> inPool;
    protected Pool<MRectangle> outPool;
    protected BoxedEscape[] threads;

    public MandelbrotRenderer(int width, int height, int numberType) {
        super(width, height, NUMBER_SYSTEMS[numberType]);

        threads = new BoxedEscape[GraphicsController.THREAD_COUNT];
        currentSystem = numberType;
        histogram = new Histogram(MAX_ITERATIONS);
        changeNumberSystem(NUMBER_SYSTEMS[currentSystem]);
        //sampler = new SuperSampleEngine(histogram, threads);
        inPool = new Pool<>();
        outPool = new Pool<>();

    }

    public void changeNumberSystem(Class numberType) {
        xCoords = (NumberType[]) Array.newInstance(numberType, xCoords.length);
        yCoords = (NumberType[]) Array.newInstance(numberType, yCoords.length);
        for (BoxedEscape thread : threads) {
            if (thread != null) {
                thread.setxCoords(xCoords);
                thread.setyCoords(yCoords);
            }
        }
        try {
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
        yCoords[0] = window.yCenter.add(window.yRange);
        for (int i = 1; i < yCoords.length; i++) {
            yCoords[i] = yCoords[i - 1].subtract(yEpsilon);
        }
        //System.out.println(xDelta);
        for (int[] d : data) {
            Arrays.fill(d, NOT_CALCULATED_CONST);
        }
        BoxedEscape.split(new MRectangle(0, 0, xCoords.length, yCoords.length), inPool);
        beginRender();
        //System.out.println("\n\n\n");
    }

    public void panRight(int distance) {
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
        BoxedEscape.split(new MRectangle(xCoords.length - distance, 0, distance, yCoords.length), inPool);
        beginRender();
        window.shiftRight(xEpsilon.multiply(distance));

    }

    public void panLeft(int distance) {
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
        BoxedEscape.split(new MRectangle(0, 0, distance, yCoords.length), inPool);
        beginRender();
        window.shiftLeft(xEpsilon.multiply(distance));

    }

    public void panDown(int distance) {
        for (int[] row : data) {
            System.arraycopy(row, distance, row, 0, row.length - distance);
            Arrays.fill(row, row.length - distance, row.length, NOT_CALCULATED_CONST);

        }
        System.arraycopy(yCoords, distance, yCoords, 0, yCoords.length - distance);
        int i;
        for (i = yCoords.length - distance; i < yCoords.length; i++) {
            yCoords[i] = yCoords[i - 1].subtract(yEpsilon);
        }

        BoxedEscape.split(new MRectangle(0, yCoords.length - distance, xCoords.length, distance), inPool);
        beginRender();
        window.shiftDown(yEpsilon.multiply(distance));

    }

    public void panUp(int distance) {
        for (int[] row : data) {
            System.arraycopy(row, 0, row, distance, row.length - distance);
            Arrays.fill(row, 0, distance, NOT_CALCULATED_CONST);
        }

        System.arraycopy(yCoords, 0, yCoords, distance, yCoords.length - distance);
        int i;
        for (i = distance - 1; i >= 0; i--) {
            yCoords[i] = yCoords[i + 1].add(yEpsilon);
        }
        BoxedEscape.split(new MRectangle(0, 0, xCoords.length, distance), inPool);
        beginRender();
        window.shiftUp(yEpsilon.multiply(distance));
    }

    protected void beginRender() {
        outPool = new Pool<>();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new BoxedEscape(xCoords, yCoords, data, histogram);
            threads[i].setInPool(inPool);
            threads[i].setOutPool(outPool);
            threads[i].setName("Drawer thread " + i);
            threads[i].start();
            System.out.println("Thread: " + threads[i].getName() + " started");

        }
        done_calculating = false;
        System.out.println(outPool.size() + " boxes");
    }

    public ArrayList<MRectangle> getBoxes() {
        return outPool.getValues();
    }

//    public void superSample(int oldFactor, int factor) {
//        long start = System.currentTimeMillis();
//        System.out.println("SAMPLING");
//        ArrayList<MRectangle> eboxes = getBoxes();
////        ArrayList<MRectangle> fboxes = new ArrayList<MRectangle>();
////        for (int i = 0; i < eboxes.size(); i++) {
////            if (eboxes.get(i).isFilled()) {
////                fboxes.add(eboxes.remove(i));
////            }
////        }
//        Pool<MRectangle> SS = sampler.computeSS(factor, data, window, eboxes);
//        //SS.addAll(eboxes);
//        outPool = SS;
//        long stop = System.currentTimeMillis();
//        System.out.println("Super sampled " + (factor * factor) + "x in " + (stop - start) + " ms");
//
//    }
    @Override
    public int getMaxIterations() {
        return MAX_ITERATIONS;
    }
    boolean done_calculating;

    @Override
    public boolean doneRendering() {
        if (threads[0] == null) {
            return true;
        }
        boolean done_calculating = true;
        for (BoxedEscape thread : threads) {
            done_calculating &= thread.getState().equals(Thread.State.TERMINATED);
        }
        return done_calculating;
    }

    @Override
    public boolean hasBoxes() {
        return true;
    }

    @Override
    public void resetWindow() {
         window = new Window(new DoubleNT(-.75), new DoubleNT(0),
                new DoubleNT(1.75), new DoubleNT(1));
    }

}
