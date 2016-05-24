package math;

import graphics.base.GraphicsController;
import graphics.colors.Histogram;
import static java.lang.Double.doubleToRawLongBits;
import java.util.Arrays;
import java.util.TreeSet;
import static math.DoubleMandelbrotCalculator.MAX_ITERATIONS;

/**
 *
 * @author David
 */
public class CalculatorThread extends Thread {

    private final Histogram histogram;
    private final int[][] buffer;
    private final double[] xCoords;
    private final double[] yCoords;
    private int miniWindow[];
    private int val;

    public CalculatorThread(Histogram histogram, double[] xCoords, double[] yCoords, int[][] buffer) {
        this.histogram = histogram;
        this.xCoords = xCoords;
        this.yCoords = yCoords;
        this.buffer = buffer;
    }

    private static final long sleepTime = Long.MAX_VALUE;

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
            }
            for (int i = miniWindow[0]; i < miniWindow[1]; i++) {
                Arrays.fill(buffer[i], miniWindow[2], miniWindow[3], -1);
            }
            render(miniWindow[0], miniWindow[1], miniWindow[2], miniWindow[3]);
        }
    }

    //variables for the escape algorithm
    private double xn, yn, xt;
    private int z;
    private final TreeSet<Integer> hashes = new TreeSet<>();

    private int escape(double x_curr, double y_curr) {

        xn = x_curr;
        yn = y_curr;
        hashes.clear();
        z = 0;
        while (z < MAX_ITERATIONS - 1) {
            if (xn * xn + yn * yn > 4) {
                //System.out.println("problem");
                return z;
            }
            if (!hashes.add(37 * hashCode(xn) + hashCode(yn))) {
                //System.out.println("saved " + (MAX_ITERATIONS - z));
                return MAX_ITERATIONS;
            }
            xt = xn * xn - yn * yn + x_curr;
            yn = 2 * xn * yn + y_curr;
            xn = xt;
            z++;
        }
        return MAX_ITERATIONS;
    }

    public void setMiniWindow(int[] minWindow) {
        this.miniWindow = minWindow;
    }

    public static int hashCode(double value) {
        long bits = doubleToRawLongBits(value);
        return (int) (bits ^ (bits >>> 32));
    }

    public int testBox(int xMin, int xMax, int yMin, int yMax) {
        int dVal;
        if (buffer[xMin][yMin] == -1) {
            buffer[xMin][yMin] = escape(xCoords[xMin], yCoords[yMin]);
        }
        if (buffer[xMax][yMin] == -1) {
            buffer[xMax][yMin] = escape(xCoords[xMax], yCoords[yMin]);
        }
        if (buffer[xMin][yMax] == -1) {
            buffer[xMin][yMax] = escape(xCoords[xMin], yCoords[yMax]);
        }
        if (buffer[xMax][yMax] == -1) {
            buffer[xMax][yMax] = escape(xCoords[xMax], yCoords[yMax]);
        }
        val = buffer[xMin][yMin];
        dVal = Math.max(Math.max(buffer[xMax][yMax], buffer[xMax][yMin]), Math.max(buffer[xMin][yMin], buffer[xMin][yMax]))
                - Math.min(Math.min(buffer[xMax][yMax], buffer[xMax][yMin]), Math.min(buffer[xMin][yMin], buffer[xMin][yMax]));
        //System.out.println(val);
        if (buffer[xMin][yMax] != val) {
            return dVal;
        }
        if (buffer[xMax][yMin] != val) {
            return dVal;
        }
        if (buffer[xMax][yMax] != val) {
            return dVal;
        }
        xLoop:
        for (int x = xMin + 1; x < xMax; x++) {
            if (buffer[x][yMin] == -1) {
                buffer[x][yMin] = escape(xCoords[x], yCoords[yMin]);
            }
            if (buffer[x][yMin] != val) {
                return dVal;
            }
            if (buffer[x][yMax] == -1) {
                buffer[x][yMax] = escape(xCoords[x], yCoords[yMax]);
            }
            if (buffer[x][yMax] != val) {
                return dVal;
            }
        }
        yLoop:
        for (int y = yMin + 1; y < yMax; y++) {
            if (buffer[xMin][y] == -1) {
                buffer[xMin][y] = escape(xCoords[xMin], yCoords[y]);
            }

            if (buffer[xMin][y] != val) {
                return dVal;
            }

            if (buffer[xMax][y] == -1) {
                buffer[xMax][y] = escape(xCoords[xMax], yCoords[y]);
            }
            if (buffer[xMax][y] != val) {
                return dVal;
            }
        }
        return -1; //0
    }

    /**
     * Updates the buffer array and the histogram;
     *
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     */
    public void iteratePlain(int xMin, int xMax, int yMin, int yMax) {
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                if (buffer[x][y] == -1) {
                    buffer[x][y] = escape(xCoords[x], yCoords[y]);
                }
                histogram.increment(buffer[x][y]);
                //System.out.println(xCurr + " " + yCurr + "i");
                //System.out.println(buffer[x][y] + "\n");
                //System.out.println(escape_value);
            }
            //System.out.println("Line " + xCurr + " \\ " + data.length);
        }
    }

    private void render(int xMin, int xMax, int yMin, int yMax) {
        int dx = xMax - xMin;
        int dy = yMax - yMin;
        boolean a = dx < 5;
        boolean b = dy < 5;
        int dVal = testBox(xMin, xMax - 1, yMin, yMax - 1); //dVal is the discrepancy between the edges, we use it to detect structure
        if (dVal == -1) { //we test if -1 because dVal can be zero but the rectangle is not valid
            //System.out.println("called");
            for (int x = xMin; x < xMax; x++) {
                for (int y = yMin; y < yMax; y++) {
                    buffer[x][y] = val;
                }
            }
            histogram.increment(val, (yMax - yMin) * (xMax - xMin));

        } else //System.out.println("not called");
         if (dVal > dx * dy * 10) {
                iteratePlain(xMin, xMax, yMin, yMax);
            } else if (a || b) {
                if (a && b) {
                    iteratePlain(xMin, xMax, yMin, yMax);
                } else if (b) {
                    render(xMin, xMin + dx / 4, yMin, yMax);
                    render(xMin + dx / 4, xMin + dx / 2, yMin, yMax);
                    render(xMin + dx / 2, xMin + 3 * dx / 4, yMin, yMax);
                    render(xMin + 3 * dx / 4, xMax, yMin, yMax);
                } else {
                    render(xMin, xMax, yMin, yMin + dy / 4);
                    render(xMin, xMax, yMin + dy / 4, yMin + dy / 2);
                    render(xMin, xMax, yMin + dy / 2, yMin + 3 * dy / 4);
                    render(xMin, xMax, yMin + 3 * dy / 4, yMax);
                }
            } else {
                //System.out.println("splitting");
                dx = xMin + dx / 2;
                dy = yMin + dy / 2;
                render(xMin, dx, yMin, dy);
                render(xMin, dx, dy, yMax);
                render(dx, xMax, yMin, dy);
                render(dx, xMax, dy, yMax);
            }
    }
}
