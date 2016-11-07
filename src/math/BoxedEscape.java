package math;

import math.numbertypes.NumberType;
import architecture.Pool;
import graphics.colors.Histogram;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import static math.MandelbrotRenderer.MAX_ITERATIONS;

/**
 *
 * @author David
 */
public class BoxedEscape extends Thread {

    private int[][] buffer;
    private NumberType[] xCoords;
    private NumberType[] yCoords;
    private int val;
    private boolean isAlive;
    public static final int NOT_CALCULATED_CONST = -1;
    private Pool<MRectangle> inPool;
    private Pool<MRectangle> outPool;
    private final Histogram histogram;

    public BoxedEscape(NumberType[] xCoords, NumberType[] yCoords, int[][] buffer, Histogram histogram) {
        this.xCoords = xCoords;
        this.yCoords = yCoords;
        this.buffer = buffer;
        this.histogram = histogram;

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
        if (buffer[e.x][e.y] == NOT_CALCULATED_CONST) {
            buffer[e.x][e.y] = escape(xCoords[e.x], yCoords[e.y]);
            histogram.increment(buffer[e.x][e.y]);
        }
        if (buffer[xMax][e.y] == NOT_CALCULATED_CONST) {
            buffer[xMax][e.y] = escape(xCoords[xMax], yCoords[e.y]);
            histogram.increment(buffer[xMax][e.y]);

        }
        if (buffer[e.x][yMax] == NOT_CALCULATED_CONST) {
            buffer[e.x][yMax] = escape(xCoords[e.x], yCoords[yMax]);
            histogram.increment(buffer[e.x][yMax]);

        }
        if (buffer[xMax][yMax] == NOT_CALCULATED_CONST) {
            buffer[xMax][yMax] = escape(xCoords[xMax], yCoords[yMax]);
            histogram.increment(buffer[xMax][yMax]);
        }
        val = buffer[e.x][e.y];
        //System.out.println(val);
        if (buffer[e.x][yMax] != val) {
            return false;
        }
        if (buffer[xMax][e.y] != val) {
            return false;
        }
        if (buffer[xMax][yMax] != val) {
            return false;
        }
        xLoop:
        for (int x = e.x + 1; x < xMax; x++) { //corners left off, should have been calculated earlier
            if (buffer[x][e.y] == NOT_CALCULATED_CONST) {
                buffer[x][e.y] = escape(xCoords[x], yCoords[e.y]);
                histogram.increment(buffer[x][e.y]);

            }
            if (buffer[x][e.y] != val) {
                return false;
            }
            if (buffer[x][yMax] == NOT_CALCULATED_CONST) {
                buffer[x][yMax] = escape(xCoords[x], yCoords[yMax]);
                histogram.increment(buffer[x][yMax]);

            }
            if (buffer[x][yMax] != val) {
                return false;
            }
        }
        yLoop:
        for (int y = e.y + 1; y < yMax; y++) {//corners left off, should have been calculated earlier
            if (buffer[e.x][y] == NOT_CALCULATED_CONST) {
                buffer[e.x][y] = escape(xCoords[e.x], yCoords[y]);
                histogram.increment(buffer[e.x][y]);

            }

            if (buffer[e.x][y] != val) {
                return false;
            }

            if (buffer[xMax][y] == NOT_CALCULATED_CONST) {
                buffer[xMax][y] = escape(xCoords[xMax], yCoords[y]);
                histogram.increment(buffer[xMax][y]);

            }
            if (buffer[xMax][y] != val) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the buffer array
     *
     * @param e
     */
    public void iteratePlain(MRectangle e) {
        for (int x = e.x; x < e.x + e.width; x++) {
            for (int y = e.y; y < e.y + e.height; y++) {

                if (buffer[x][y] == NOT_CALCULATED_CONST) {
                    buffer[x][y] = escape(xCoords[x], yCoords[y]);
                    histogram.increment(buffer[x][y]);
                }

                //System.out.println(xCurr + " " + yCurr + "i");
                //System.out.println(buffer[x][y] + "\n");
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
                for (int y = e.y; y < e.y + e.height; y++) {
                    buffer[x][y] = val;
                }
            }
            histogram.increment(buffer[e.x][e.y], area);
            e.setPixelCalculated(false);
            outPool.add(e);
            return;
        }
        if (area < 25) {
            iteratePlain(e);
            e.setPixelCalculated(true);
            outPool.add(e);
            return;
        }
        //dVal is the discrepancy between the edges, we use it to detect structure
        a = Math.max(buffer[e.x][e.y], buffer[e.x + e.width - 1][e.y]);
        b = Math.min(buffer[e.x][e.y], buffer[e.x + e.width - 1][e.y]);

        c = Math.max(buffer[e.x + e.width - 1][e.y + e.height - 1], buffer[e.x][e.y + e.height - 1]);
        d = Math.min(buffer[e.x + e.width - 1][e.y + e.height - 1], buffer[e.x][e.y + e.height - 1]);

        //maximum difference
        if (Math.max(a, c) - Math.min(b, d) > area && area < 100) {
            //System.out.println((a - b) + "\t" + e.width + e.height);
            iteratePlain(e);
            e.setPixelCalculated(true);
            outPool.add(e);
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


    public void setxCoords(NumberType[] xCoords) {
        this.xCoords = xCoords;
    }

    public void setyCoords(NumberType[] yCoords) {
        this.yCoords = yCoords;
    }

    public void setInPool(Pool<MRectangle> inPool) {
        this.inPool = inPool;
    }

    public void setOutPool(Pool<MRectangle> outPool) {
        this.outPool = outPool;
    }

    public void setBuffer(int[][] buffer) {
        this.buffer = buffer;
    }

    public void kill() {
        this.isAlive = false;
    }

}
