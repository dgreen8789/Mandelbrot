/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

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
    private final int[][] segments;
    //xMinPixel, xMaxPixel, yMinPixel, yMaxPixel
    private int miniWindow[];

    public CalculatorThread(Histogram histogram, double[] xCoords, double[] yCoords, int[][] buffer) {
        this.histogram = histogram;
        this.xCoords = xCoords;
        this.yCoords = yCoords;
        this.buffer = buffer;
        this.segments = new int[yCoords.length][2];
    }

    private static final long sleepTime = Long.MAX_VALUE;
    private int x0, yi, traceVal;

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
            }
            printInfo();
            int x;
            for (x = miniWindow[1] - 1; x >= miniWindow[0]; x--) {
                Arrays.fill(buffer[x], -1);
            }

            int y = miniWindow[2];
            x++;
            while (y < miniWindow[3] && buffer[x][y] == -1) {
                traceVal = buffer[x][y] = escape(xCoords[x], yCoords[y]);
                histogram.increment(traceVal);
                x0 = x++;
                while (x < miniWindow[1]) {
                    buffer[x][y] = escape(xCoords[x], yCoords[y]);
                    histogram.increment(buffer[x][y]);
                    if (buffer[x][y] != traceVal) {
                        x++;
                        break;
                    }
                    x++;
                }
                yi = trace(x0, x, y + 1);                
                while(yi > miniWindow(1))
                
                if (x == miniWindow[1]) {
                    x = miniWindow[0];
                    y++;
                }

            }

            // System.out.println("GOING");
//            for (x = miniWindow[0]; x < miniWindow[1]; x++) {
//                for (y = miniWindow[2]; y < miniWindow[3]; y++) {
//                    if (buffer[x][y] == -1) {
//                        //System.out.println("CLEANUP:" + x + ", " + y);
//                        buffer[x][y] = escape(xCoords[x], yCoords[y]);
//                        histogram.increment(buffer[x][y]);
//                    }
//                    //System.out.println(xCurr + " " + yCurr + "i");
//                    //System.out.println(buffer[x][y] + "\n");
//                    //System.out.println(escape_value);
//                }
//                //System.out.println("Line " + xCurr + " \\ " + data.length);
//            }
        }
    }

    private int trace(int x0, int xf, int y) {
        if (xf >= 1360 || x0 < 0) {
            System.out.println("bad bounds in X");
            return y;
        }
        if (xf - x0 < 3) {
            System.out.printf("Segment of length < 3 %d(incl.) to %d(incl.) with escape value %d at y = %d identified. Returning...\n", x0, xf, traceVal,y);
            return y;
        } else {
            System.out.printf("Segment %d(incl.) to %d(incl.) with escape val %d at y = %d identified\n", x0, xf, traceVal, y);
        }
        if (y == miniWindow[3]) {
            //System.out.println("Reached rendering bounds. Returning...");
            return y;
        }
        segments[y][0] = segments[y][1] = -1;
        buffer[x0][y] = escape(xCoords[x0], yCoords[y]);
        buffer[xf][y] = escape(xCoords[xf], yCoords[y]);
        if (buffer[x0][y] == traceVal) {
            if (x0 > miniWindow[0]) {
                x0 = lookLeft(x0, miniWindow[0], y, traceVal);
            }

        } else {
            histogram.increment(buffer[x0][y]);
            x0 = lookRight(x0, xf, y, traceVal);

        }
        if (buffer[xf][y] == traceVal) {
            if (xf < miniWindow[1]) {
                xf = lookRight(xf, miniWindow[1], y, traceVal);

            }
        } else {
            histogram.increment(buffer[xf][y]);
            xf = lookLeft(xf, x0, y, traceVal);
        }
        segments[y][0] = x0;
        segments[y][1] = xf;
        if (segments[y][0] == -1 || segments[y][1] == -1) {
            System.out.println("Area detected to have ended.");
            return y - 1;
        }
        return trace(segments[y][0], segments[y][1], y + 1);
    }

    private int lookRight(int startX, int stopX, int y, int val) {
        while (startX < stopX) {
            int eVal = escape(xCoords[startX], yCoords[y]);
            if (eVal != val) {
                return startX;
            }
            startX++;
        }
        return -1;
    }

    private int lookLeft(int startX, int stopX, int y, int val) {
        while (startX > stopX) {
            int eVal = escape(xCoords[startX], yCoords[y]);
            if (eVal != val) {
                return startX;
            }
            startX--;
        }
        return -1;
    }
    //variables for the escape algorithm
    private double xn;
    private double yn;
    private double xt;
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

    public synchronized void printInfo() {
        System.out.println("Thread: " + Thread.currentThread().getName() + " drawing from" + "\n"
                + "X: " + miniWindow[0] + " to " + miniWindow[1] + "\n"
                + "Y: " + miniWindow[2] + " to " + miniWindow[3]);

    }

    private int findBottom() {
    }

}
