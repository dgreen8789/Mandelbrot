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

/**
 *
 * @author David
 */
public class MandelbrotRenderer extends FractalRenderer {

    public static int MAX_ITERATIONS = 16384;
    public static double HARD_ZOOM_FACTOR = 10.0;
    public static double SOFT_ZOOM_FACTOR = 1.5;
    private MRectangle screen;
    protected Pool<MRectangle> inPool;
    protected Pool<MRectangle> outPool;
    protected BoxedEscape[] threads;

    public MandelbrotRenderer(int width, int height) {
        super(width, height);
        threads = new BoxedEscape[GraphicsController.THREAD_COUNT];
        histogram = new Histogram(MAX_ITERATIONS);
        //sampler = new SuperSampleEngine(histogram, threads);
        inPool = new Pool<>();
        outPool = new Pool<>();
        screen = new MRectangle(false, 0, 0, width, height);
        for (BoxedEscape thread : threads) {
            if (thread != null) {
                thread.setxCoords(xCoords);
                thread.setyCoords(yCoords);
            }
        }

    }

    protected MandelbrotRenderer(int width, int height, Class theClass) {
        super(width, height, theClass);
        histogram = new Histogram(MAX_ITERATIONS);
        inPool = new Pool<>();
        outPool = new Pool<>();
        screen = new MRectangle(false, 0, 0, width, height);
    }

    @Override
    public void changeNumberSystem(boolean up) {
        window = changeWindow(up, window);
        xEpsilon = window.xRange.divide(data.length).mult2();
        yEpsilon = window.yRange.divide(data[0].length).mult2();
        Class nextType = up ? xCoords[0].toNextSystem().getClass() : xCoords[0].toPreviousSystem().getClass();
        xCoords = (NumberType[]) Array.newInstance(nextType, xCoords.length);
        yCoords = (NumberType[]) Array.newInstance(nextType, yCoords.length);
        for (BoxedEscape thread : threads) {
            if (thread != null) {
                thread.setxCoords(xCoords);
                thread.setyCoords(yCoords);
            }
        }
        outPool.clear();
        System.out.println("Switched to number system " + nextType.getSimpleName());
    }

    public void zoom(boolean deeper, Point p, double factor) {
        NumberType[] coords = coordinateByPoint(p);
        if (!deeper) {
            factor = 1 / factor;
        }
        window.zoom(coords[0], coords[1], factor);
        System.out.println(window);
        long start = System.currentTimeMillis();
        scale(factor, p, outPool, data, valid);
        long stop = System.currentTimeMillis();
        System.out.println("Scaling took " + (stop - start) + " ms");
        draw();
    }

    public void draw() {
        //System.out.println("called");
        System.out.println(window);
        createCoordinateGrid(window, xCoords, yCoords);

        //System.out.println("coordinate arrays check");
        //System.out.println(xDelta);
        //inPool.clear();
        //MRectangle.split(new MRectangle(0, 0, xCoords.length, yCoords.length), inPool);
        //outPool = new Pool<>();
        //System.out.println("pools set up");
        beginRender(true, 0, 0, xCoords.length, yCoords.length);

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
            Arrays.fill(valid[i], false);
            xCoords[i] = xCoords[i - 1].add(xEpsilon);
        }
        //MRectangle.split(new MRectangle(xCoords.length - distance, 0, distance, yCoords.length), inPool);
        histogram.getValidPixelCounter().addAndGet(-(distance * yCoords.length));
        panRectangles(-distance, 0);
        window.shiftRight(xEpsilon.multiply(distance));
        beginRender(false, xCoords.length - distance, 0, distance, yCoords.length);

    }

    public void panLeft(int distance) {
        int i;
        for (i = xCoords.length - distance - 1; i >= 0; i--) {
            xCoords[i + distance] = xCoords[i];
            data[i + distance] = data[i];
        }
        for (i = distance - 1; i >= 0; i--) {
            data[i] = new int[data[0].length];
            Arrays.fill(valid[i], false);
            xCoords[i] = xCoords[i + 1].subtract(xEpsilon);
        }
        //MRectangle.split(new MRectangle(0, 0, distance, yCoords.length), inPool);
        histogram.getValidPixelCounter().addAndGet(-(distance * yCoords.length));
        panRectangles(distance, 0);
        window.shiftLeft(xEpsilon.multiply(distance));
        beginRender(false, 0, 0, distance, yCoords.length);

    }

    public void panDown(int distance) {
        for (int[] row : data) {
            System.arraycopy(row, distance, row, 0, row.length - distance);
        }
        for (boolean[] v : valid) {
            System.arraycopy(v, distance, v, 0, v.length - distance);
            Arrays.fill(v, v.length - distance, v.length, false);
        }
        System.arraycopy(yCoords, distance, yCoords, 0, yCoords.length - distance);
        int i = 0;
        for (i = yCoords.length - distance; i < yCoords.length; i++) {
            yCoords[i] = yCoords[i - 1].subtract(yEpsilon);
        }
        panRectangles(0, -distance);
        histogram.getValidPixelCounter().addAndGet(-(distance * xCoords.length));

        //MRectangle.split(new MRectangle(0, yCoords.length - distance, xCoords.length, distance), inPool);
        window.shiftDown(yEpsilon.multiply(distance));
        beginRender(false, 0, yCoords.length - distance, xCoords.length, distance);

    }

    public void panUp(int distance) {
        for (int[] row : data) {
            System.arraycopy(row, 0, row, distance, row.length - distance);
        }
        for (boolean[] v : valid) {
            System.arraycopy(v, 0, v, distance, v.length - distance);
            Arrays.fill(v, 0, distance, false);
        }
        System.arraycopy(yCoords, 0, yCoords, distance, yCoords.length - distance);
        int i;
        for (i = distance - 1; i >= 0; i--) {
            yCoords[i] = yCoords[i + 1].add(yEpsilon);
        }
        panRectangles(0, distance);
        histogram.getValidPixelCounter().addAndGet(-(distance * xCoords.length));
        //MRectangle.split(new MRectangle(0, 0, xCoords.length, distance), inPool);
        window.shiftUp(yEpsilon.multiply(distance));
        beginRender(false, 0, 0, xCoords.length, distance);
    }

    @Override
    protected void beginRender(boolean killThreads, int x0, int y0, int width, int height) {
        //System.out.println("started render");
        //inPool.clear();
//        MRectangle c = new MRectangle(x0, y0, width, height);
//        System.out.println(c);
//        System.out.println(c.isEmpty());
//        MRectangle.split(c, inPool);
        MRectangle.split(new MRectangle(x0, y0, width, height), inPool);
        System.out.println(window);
        for (int i = 0; i < threads.length; i++) {
            if (killThreads) {
                if (threads[i] != null) {
                    threads[i].kill();
                }
                threads[i] = new BoxedEscape(xCoords, yCoords, data, valid, histogram);
                threads[i].setInPool(inPool);
                threads[i].setOutPool(outPool);
                threads[i].setName("Drawer thread " + i);
                threads[i].start();
            } else if (threads[i].getState() == Thread.State.TIMED_WAITING) {
                threads[i].interrupt();
            }
        }
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

    @Override
    public boolean hasBoxes() {
        return true;
    }

    @Override
    public void resetWindow() {
        window = new Window(new DoubleNT(-.75), new DoubleNT(0),
                new DoubleNT(1.75), new DoubleNT(1));
    }

    private synchronized void scale(double factor, Point newCenter, Pool<MRectangle> recs, int[][] out, boolean[][] boolOut) {
        //MRectangle newScreen = 
        MRectangle screen = this.screen;
        final int shiftX0 = (screen.x + screen.width) / 2 - newCenter.x;
        final int shiftY0 = (screen.y + screen.height) / 2 - newCenter.y;
        final double oneMinusFactorCenterX = (1 - factor) * (screen.x + screen.width) / 2;
        final double oneMinusFactorCenterY = (1 - factor) * (screen.y + screen.height) / 2;
        System.out.println("Scaling factor is " + factor + " about " + newCenter);
        System.out.println(recs.size());
        recs.forEach(rect -> {
            if (useful(rect)) {
                if (edgeChecks(data, rect, rect.getValue(), screen)) {
                    rect.setLocation(rect.x + shiftX0, rect.y + shiftY0);
                } else {
                    rect.setBounds(rect.x + shiftX0 + 1, rect.y + shiftY0 + 1, rect.width - 2, rect.height - 2);
                }
                rect.setFrameFromDiagonal(
                        factor * rect.x + oneMinusFactorCenterX,
                        factor * rect.y + oneMinusFactorCenterY,
                        factor * (rect.x + rect.width) + oneMinusFactorCenterX,
                        factor * (rect.y + rect.height) + oneMinusFactorCenterY);
                rect.selfIntersection(screen);
                rect.setIsHistorical(true);
            }
        });
        for (boolean[] b : boolOut) {
            Arrays.fill(b, false);
        }

        histogram.getValidPixelCounter().set(0);
        recs.forEach(rect -> {
            if (!rect.isEmpty() && rect.isHistorical()) {
                for (int x = rect.x; x < rect.x + rect.width; x++) {
                    Arrays.fill(out[x], rect.y, rect.y + rect.height, rect.getValue());
                    Arrays.fill(boolOut[x], rect.y, rect.y + rect.height, true);
                }
            }
        });
        recs.clear();
        //recs.addAll(Arrays.asList(rectArr).);
    }

    private boolean useful(MRectangle rect) {
        return !rect.isPixelCalculated() && rect.width > 2 && rect.height > 2;
    }

    public void hardResetData() {
        for (boolean[] b : valid) {
            Arrays.fill(b, false);
        }
        for (int[] d : data) {
            Arrays.fill(d, 0);
        }
    }

    //values in pixelspace +y is down
    private void panRectangles(int dx, int dy) {
        outPool.forEach(p -> {
            p.setLocation(p.x + dx, p.y + dy);
            p.selfIntersection(screen);
        });
    }

    private static boolean edgeChecks(int[][] data, MRectangle rect, int value, MRectangle screen) {

        int i;
        boolean le, re, te, be, lec, rec, tec, bec;
        le = rect.x > 1;
        re = rect.x + rect.width < screen.width - 1;
        te = rect.y > 1;
        be = rect.y + rect.height < screen.height - 1;
        int yMin = rect.y - 1;
        int yMax = yMin + rect.height + 2;
        int xMin = rect.x - 1;
        int xMax = xMin + rect.width + 2;
        //System.out.println(screen);
        //System.out.println(rect);
        if (re && le) {

            i = xMin;
            if (te) {
                while (data[i][yMin] == value && (i < xMax)) {
                    i++;
                }
            }
            tec = i == xMax;
            i = xMin;
            if (be) {
                while (data[i][yMax] == value && (i < xMax)) {
                    i++;
                }
            }
            bec = i == xMax;
        } else {
            tec = bec = false;
        }
        if (te && be) {
            i = yMin;
            if (le) {
                while (data[rect.x - 1][i] == value && i < yMax) {
                    i++;
                }
            }
            lec = i == yMax;
            i = yMin;
            if (re) {
                while (data[xMax][i] == value && i < yMax) {
                    i++;
                }
            }
            rec = i == yMax;
        } else {
            lec = rec = false;
        }
        return tec && rec && bec && lec;
    }

    @Override
    public synchronized void resize(int newX, int newY) {
        inPool.clear();
        outPool.clear();
        double factor = Math.max(((double) newX) / screen.width, ((double) newY) / screen.height);
        int[][] newData = new int[newX][newY];
        boolean[][] newValid = new boolean[newX][newY];
        NumberType[] newXCoords = new NumberType[newX];
        NumberType[] newYCoords = new NumberType[newY];
        scale(factor, new Point(newX / 2, newY / 2), outPool, newData, newValid);
        screen.setBounds(0, 0, newX, newY);
        createCoordinateGrid(window, newXCoords, newYCoords);
        xCoords = newXCoords;
        yCoords = newYCoords;
        if (threads != null) {
            for (BoxedEscape e : threads) {
                if (e != null) {
                    e.setData(newData);
                    e.setValid(newValid);
                    e.setxCoords(newXCoords);
                    e.setyCoords(newYCoords);
                }
            }
        }
        data = newData;
        valid = newValid;
        beginRender(true, 0, 0, xCoords.length, yCoords.length);

    }

    private void createCoordinateGrid(Window window, NumberType[] xCoords, NumberType[] yCoords) {
        xEpsilon = window.xRange.divide(data.length).mult2();
        yEpsilon = window.yRange.divide(data[0].length).mult2();
        //System.out.println("epsilon initialization completed");
        System.out.println(xCoords.getClass());
        xCoords[0] = window.xCenter.subtract(window.xRange);
        for (int i = 1; i < xCoords.length; i++) {
            xCoords[i] = xCoords[i - 1].add(xEpsilon);
        }
        yCoords[0] = window.yCenter.add(window.yRange);
        for (int i = 1; i < yCoords.length; i++) {
            yCoords[i] = yCoords[i - 1].subtract(yEpsilon);
        }
    }

}
