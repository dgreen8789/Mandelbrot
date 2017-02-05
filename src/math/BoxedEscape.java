package math;

import math.numbertypes.NumberType;
import graphics.colors.Histogram;
import java.util.Arrays;
import java.util.HashSet;
import static math.MandelbrotRenderer.MAX_ITERATIONS;

/**
 *
 * @author David
 */
public class BoxedEscape extends RenderMethod {

    private int val;

    public BoxedEscape(NumberType[] xCoords, NumberType[] yCoords, int[][] buffer, boolean[][] valid, Histogram histogram) {
        super(xCoords, yCoords, buffer, valid, histogram);
    }

    @Override
    public synchronized void run() {
        isAlive = true;
        while (isAlive) {
            while (isAlive && !inPool.isEmpty()) {
                try {
                    render(inPool.remove(inPool.size() - 1));
                } catch (IndexOutOfBoundsException e) {
                    //System.out.println("tried to remove from empty pool");
                }
            }
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {

            }
        }
    }

    protected int escape(NumberType x_curr, NumberType y_curr) {
        //Making new hashset is faster than an clearing existing hashset. Thanks, Thomas
        return x_curr.mEscape(x_curr, y_curr, new HashSet<>(), MAX_ITERATIONS);
    }

    public boolean testBox(MRectangle e) {
//        //System.out.println(e.height);
        int yMax = e.y + e.height - 1;
        //System.out.println("ymax " +  yMax);
        int xMax = e.x + e.width - 1;
        if (!valid[e.x][e.y]) {
            data[e.x][e.y] = escape(xCoords[e.x], yCoords[e.y]);
            histogram.increment(data[e.x][e.y]);
            valid[e.x][e.y] = true;
        }
        if (!valid[xMax][e.y]) {
            data[xMax][e.y] = escape(xCoords[xMax], yCoords[e.y]);
            histogram.increment(data[xMax][e.y]);
            valid[xMax][e.y] = true;

        }
        if (!valid[e.x][yMax]) {
            data[e.x][yMax] = escape(xCoords[e.x], yCoords[yMax]);
            histogram.increment(data[e.x][yMax]);
            valid[e.x][yMax] = true;

        }
        if (!valid[xMax][yMax]) {
            data[xMax][yMax] = escape(xCoords[xMax], yCoords[yMax]);
            histogram.increment(data[xMax][yMax]);
            valid[xMax][yMax] = true;
        }
        val = data[e.x][e.y];
        //System.out.println(val);
        if (data[e.x][yMax] != val) {
            return false;
        }
        if (data[xMax][e.y] != val) {
            return false;
        }
        if (data[xMax][yMax] != val) {
            return false;
        }
        xLoop:
        for (int x = e.x + 1; x < xMax; x++) { //corners left off, should have been calculated earlier
            if (!valid[x][e.y]) {
                data[x][e.y] = escape(xCoords[x], yCoords[e.y]);
                histogram.increment(data[x][e.y]);
                valid[x][e.y] = true;

            }
            if (data[x][e.y] != val) {
                return false;
            }
            if (!valid[x][yMax]) {
                data[x][yMax] = escape(xCoords[x], yCoords[yMax]);
                histogram.increment(data[x][yMax]);
                valid[x][yMax] = true;

            }
            if (data[x][yMax] != val) {
                return false;
            }
        }
        yLoop:
        for (int y = e.y + 1; y < yMax; y++) {//corners left off, should have been calculated earlier
            if (!valid[e.x][y]) {
                data[e.x][y] = escape(xCoords[e.x], yCoords[y]);
                histogram.increment(data[e.x][y]);
                valid[e.x][y] = true;
            }

            if (data[e.x][y] != val) {
                return false;
            }

            if (!valid[xMax][y]) {
                data[xMax][y] = escape(xCoords[xMax], yCoords[y]);
                histogram.increment(data[xMax][y]);
                valid[xMax][y] = true;
            }
            if (data[xMax][y] != val) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the data array
     *
     * @param e
     */
    public void iteratePlain(MRectangle e) {
        for (int x = e.x; x < e.x + e.width; x++) {
            for (int y = e.y; y < e.y + e.height; y++) {

                if (!valid[x][y]) {
                    data[x][y] = escape(xCoords[x], yCoords[y]);
                    histogram.increment(data[x][y]);
                    valid[x][y] = true;
                }

                //System.out.println(xCurr + " " + yCurr + "i");
                //System.out.println(data[x][y] + "\n");
                //System.out.println(escape_value);
            }
            //System.out.println("Line " + xCurr + " \\ " + data.length);
        }

    }

    private void render(MRectangle e) {
        int a, b, c, d, area;
        area = e.width * e.height;
        if (testBox(e)) {
            for (int x = e.x; x < e.x + e.width; x++) {
                Arrays.fill(data[x], e.y, e.y + e.height, val);
                Arrays.fill(valid[x], e.y, e.y + e.height, true);
            }
            histogram.increment(data[e.x][e.y], area);
            e.setPixelCalculated(false);
            e.setValue(val);
            outPool.add(e);
            histogram.getValidPixelCounter().addAndGet(area);
            return;
        }
        if (area < 25) {
            iteratePlain(e);
            e.setPixelCalculated(true);
            outPool.add(e);
            histogram.getValidPixelCounter().addAndGet(area);
            return;
        }
        //dVal is the discrepancy between the edges, we use it to detect structure
        a = Math.max(data[e.x][e.y], data[e.x + e.width - 1][e.y]);
        b = Math.min(data[e.x][e.y], data[e.x + e.width - 1][e.y]);

        c = Math.max(data[e.x + e.width - 1][e.y + e.height - 1], data[e.x][e.y + e.height - 1]);
        d = Math.min(data[e.x + e.width - 1][e.y + e.height - 1], data[e.x][e.y + e.height - 1]);

        //maximum difference
        if (Math.max(a, c) - Math.min(b, d) > area && area < 100) {
            //System.out.println((a - b) + "\t" + e.width + e.height);
            iteratePlain(e);
            e.setPixelCalculated(true);
            outPool.add(e);
            histogram.getValidPixelCounter().addAndGet(area);
        } else {
            MRectangle.split(e, inPool);
            //System.out.println(inPool.getValues());
        }
//        try {
//            //slows renderer down
//            Thread.sleep(25);
//        } catch (InterruptedException ex) {
//        }
    }
}
