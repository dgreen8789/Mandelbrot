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
    public static double HARD_ZOOM_FACTOR = 10.0;
    public static double SOFT_ZOOM_FACTOR = 1.1;
    private final MRectangle SCREEN;
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
        SCREEN = new MRectangle(false, 0, 0, width, height);
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

    public void zoom(boolean deeper, Point p, double factor) {
        NumberType[] coords = coordinateByPoint(p);
        if (!deeper) {
            factor = 1 / factor;
        }
        if (factor > 1) {
            window.zoomIn(coords[0], coords[1], factor);
        } else {
            window.zoomOut(coords[0], coords[1], factor);
        }
        if (factor > 10) {
            System.out.println(window);
        }
        if (factor < .1) {
            for (int[] data1 : data) {
                Arrays.fill(data1, NOT_CALCULATED_CONST);
            }
        } else {
            scale(data, factor, p, outPool);
        }
        draw();
    }

    public void draw() {
        //System.out.println("called");
        //System.out.println(window);
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
        //System.out.println("epsilon initialization completed");
        xCoords[0] = window.xCenter.subtract(window.xRange);
        for (int i = 1; i < xCoords.length; i++) {
            xCoords[i] = xCoords[i - 1].add(xEpsilon);
        }
        yCoords[0] = window.yCenter.add(window.yRange);
        for (int i = 1; i < yCoords.length; i++) {
            yCoords[i] = yCoords[i - 1].subtract(yEpsilon);
        }
        //System.out.println("coordinate arrays check");
        //System.out.println(xDelta);
        //inPool.clear();

        MRectangle.split(new MRectangle(0, 0, xCoords.length, yCoords.length), inPool);
        //outPool = new Pool<>();
        //System.out.println("pools set up");
        beginRender(true);

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
        MRectangle.split(new MRectangle(xCoords.length - distance, 0, distance, yCoords.length), inPool);
        panRectangles(-distance, 0);
        window.shiftRight(xEpsilon.multiply(distance));
        beginRender(false);

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
        MRectangle.split(new MRectangle(0, 0, distance, yCoords.length), inPool);
        panRectangles(distance, 0);
        window.shiftLeft(xEpsilon.multiply(distance));
        beginRender(false);

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
        panRectangles(0, -distance);

        MRectangle.split(new MRectangle(0, yCoords.length - distance, xCoords.length, distance), inPool);
        window.shiftDown(yEpsilon.multiply(distance));
        beginRender(false);

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
        panRectangles(0, distance);
        MRectangle.split(new MRectangle(0, 0, xCoords.length, distance), inPool);
        window.shiftUp(yEpsilon.multiply(distance));
        beginRender(false);
    }

    protected void beginRender(boolean killThreads) {
        //System.out.println("started render");
        System.out.println(window);
        for (int i = 0; i < threads.length; i++) {
            if (killThreads) {
                if (threads[i] != null) {
                    threads[i].kill();
                }
                threads[i] = new BoxedEscape(xCoords, yCoords, data, histogram);
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

    private void scale(int[][] data, double factor, Point newCenter, Pool<MRectangle> recs) {
        System.out.println("Scaling factor is " + factor + " about " + newCenter);
        //MRectangle newScreen = 
        ArrayList<MRectangle> alist = recs.getValues();
        MRectangle rect;
        int value;
        ArrayList<Integer> values = new ArrayList<>();
        System.out.println(alist.size());
        int shiftX0 = (SCREEN.x + SCREEN.width) / 2 - newCenter.x;
        int shiftY0 = (SCREEN.y + SCREEN.height) / 2 - newCenter.y;
        double oneMinusFactorCenterX = (1 - factor) * (SCREEN.x + SCREEN.width) / 2;
        double oneMinusFactorCenterY = (1 - factor) * (SCREEN.y + SCREEN.height) / 2;
        boolean[] edgeChecks = new boolean[4]; //top, bottom, left, right;
        for (int i = alist.size() - 1; i > - 1; i--) {
            rect = alist.get(i);
            //rect.width > (b / a) && rect.height > (b / a) && 
            boolean cond = !rect.isPixelCalculated();//&& rect.width > factor && rect.height > factor;
            //System.out.println(rect + "-->" + cond);
            if (cond) {
                MRectangle r0 = (MRectangle) rect.clone();
                value = data[rect.x + rect.width / 2][rect.y + rect.height / 2];
                // System.out.println("Old: " + rect);
                rect.translate(shiftX0, shiftY0);
                rect.setFrameFromDiagonal(
                        factor * rect.x + oneMinusFactorCenterX,
                        factor * rect.y + oneMinusFactorCenterY,
                        factor * (rect.x + rect.width) + oneMinusFactorCenterX,
                        factor * (rect.y + rect.height) + oneMinusFactorCenterY);
                //System.out.println("Scaled: " + rect);
                //System.out.println("Scaled: " + rect);

                rect = rect.intersection(SCREEN);
                if (rect.isEmpty()) {
                    alist.remove(i);
                } else //System.out.println("Intersection: " + rect + "\n");
                {
                    edgeChecks[0] = edgeChecks[1] = edgeChecks[2] = edgeChecks[3] = false;
                    edgeChecks(data, r0, edgeChecks, value);
                    //System.out.println("Edge checking yielded: " + Arrays.toString(edgeChecks));
                    if ((edgeChecks[0] && edgeChecks[1] && edgeChecks[2] && edgeChecks[3])) {
                        rect.setPixelCalculated(false);
                        rect.setIsHistorical(true);
                        values.add(0, value);
                        alist.set(i, rect);
                    } else {
                        alist.remove(i);
                    }// System.out.println("Started with " + r0);
                }                //System.out.println("Edge checking yielded: " + Arrays.toString(edgeChecks));              //System.out.println("Kept Rect: " + rect);
            } else {
                alist.remove(i);
            }
        }
        for (int[] d : data) {
            Arrays.fill(d, NOT_CALCULATED_CONST);
        }
        for (int i = 0; i < alist.size(); i++) {
            rect = alist.get(i);
            //System.out.println(rect);
            for (int x = rect.x; x < rect.x + rect.width; x++) {
                for (int y = rect.y; y < rect.y + rect.height; y++) {
                    data[x][y] = values.get(i);
                }
            }

        }
        recs.clear();
        recs.addAll(alist);

    }

    //values in pixelspace +y is down
    private void panRectangles(int dx, int dy) {
        for (MRectangle r : outPool.getValues()) {
            r.translate(dx, dy);
            r = r.intersection(SCREEN);
        }
    }

    private void edgeChecks(int[][] data, MRectangle rect, boolean[] b, int value) {
        int i;
        boolean le, re, te, be;
        le = rect.x > 1;
        re = rect.x + rect.width < SCREEN.width - 1;
        te = rect.y > 1;
        be = rect.y + rect.height < SCREEN.height - 1;
        int yMin = rect.y - 1;
        int yMax = yMin + rect.height + 2;
        int xMin = rect.x - 1;
        int xMax = xMin + rect.width + 2;
        if (re && le) {

            i = xMin;
            if (te) {
                while (data[i][yMin] == value && (i < xMax)) {
                    i++;
                }
            }
            b[0] = i == xMax;
            i = xMin;
            if (be) {
                while (data[i][yMax] == value && (i < xMax)) {
                    i++;
                }
            }
            b[1] = i == xMax;
        } else {
            b[0] = b[1] = false;
        }
        if (te && be) {
            i = yMin;
            if (le) {
                while (data[rect.x - 1][i] == value && i < yMax) {
                    i++;
                }
            }
            b[2] = i == yMax;
            i = yMin;
            if (re) {
                while (data[xMax][i] == value && i < yMax) {
                    i++;
                }
            }
            b[3] = i == yMax;
        } else {
            b[2] = b[3] = false;
        }
    }

}
