/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import graphics.colors.Histogram;
import static java.lang.Double.doubleToRawLongBits;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.TreeSet;
import static math.DoubleMandelbrotCalculator.MAX_ITERATIONS;
import sun.misc.DoubleConsts;

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
            //System.out.println("GOING");
            for (int x = miniWindow[0]; x < miniWindow[1]; x++) {
                for (int y = miniWindow[2]; y < miniWindow[3]; y++) {
                    buffer[x][y] = escape(xCoords[x], yCoords[y]);
                    histogram.increment(buffer[x][y]);
                    //System.out.println(xCurr + " " + yCurr + "i");
                    //System.out.println(buffer[x][y] + "\n");
                    //System.out.println(escape_value);
                }
                //System.out.println("Line " + xCurr + " \\ " + data.length);
            }
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
    
}
