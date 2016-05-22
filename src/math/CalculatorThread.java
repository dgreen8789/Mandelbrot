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
    //xMinPixel, xMaxPixel, yMinPixel, yMaxPixel
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
                Arrays.fill(buffer[i], -1);
            }
            render(miniWindow[0], miniWindow[1], miniWindow[2], miniWindow[3]);
            
        }
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

    public boolean testBox(int xMin, int xMax, int yMin, int yMax) {
        if(xMax - xMin < 2 || yMax - yMin < 2) return false;
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
        //System.out.println(val);
        if (buffer[xMin][yMax] != val) {
            return false;
        }
        if (buffer[xMax][yMin] != val) {
            return false;
        }
        if (buffer[xMax][yMax] != val) {
            return false;
        }
        xLoop:
        for (int x = xMin + 1; x < xMax; x++) {
            if (buffer[x][yMin] == -1) {
                buffer[x][yMin] = escape(xCoords[x], yCoords[yMin]);
            }
            if (buffer[x][yMin] != val) {
                return false;
            }
            if (buffer[x][yMax] == -1) {
                buffer[x][yMax] = escape(xCoords[x], yCoords[yMax]);
            }
            if (buffer[x][yMax] != val) {
                return false;
            }
        }
        yLoop:
        for (int y = yMin + 1; y < yMax; y++) {
            if (buffer[xMin][y] == -1) {
                buffer[xMin][y] = escape(xCoords[xMin], yCoords[y]);
            }

            if (buffer[xMin][y] != val) {
                return false;
            }
              
            if (buffer[xMax][y] == -1) {
                buffer[xMax][y] = escape(xCoords[xMax], yCoords[y]);
              

            }
            if (buffer[xMax][y] != val) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the buffer array and the histogram;
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
        boolean a, b;
        int dx, dy;
        if (testBox(xMin, xMax - 1, yMin, yMax - 1)) {
            //System.out.println("called");
            for (int x = xMin; x < xMax; x++) {
                //Arrays.fill(buffer[x], yMin, yMax, val);
            }
            histogram.increment(val, (yMax - yMin) * (xMax - xMin));
        } else {
            //System.out.println("not called");
            dx = xMax - xMin;
            dy = yMax - yMin;
            a = dx < 5;
            b = dy < 5;
            if (a || b) {
                if (a && b) {
                    iteratePlain(xMin, xMax, yMin, yMax);
                    return;
                } else {
                    if (b) {
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
                    return;
                }
            }
            //System.out.println("splitting");
            dx = xMin + dx /2;
            dy = yMin + dy /2;
            render(xMin, dx, yMin, dy);
            render(xMin, dx, dy, yMax);
            render(dx, xMax, yMin, dy);
            render(dx, xMax, dy, yMax);

        }
    }

}
