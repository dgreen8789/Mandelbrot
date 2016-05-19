/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import graphics.colors.Histogram;
import java.awt.Point;
import static java.lang.Double.doubleToRawLongBits;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import static math.DoubleMandelbrotCalculator.MAX_ITERATIONS;
import sun.misc.DoubleConsts;

/**
 *
 * @author David
 */
public class CalculatorThread extends Thread {

    private final Histogram histogram;
    private int[][] buffer;
    private double[] xCoords;
    private double[] yCoords;
    //xMinPixel, xMaxPixel, yMinPixel, yMaxPixel
    private int miniWindow[];

    public CalculatorThread(Histogram histogram) {
        this.histogram = histogram;
        PARTY_TIME = false;
    }

    private volatile boolean PARTY_TIME;
    private volatile long sleepTime = Long.MAX_VALUE;
    @Override
    public void run() {
        while (true) {
            if (PARTY_TIME) {
                //System.out.println("GOING");
                for (int x = miniWindow[0]; x < miniWindow[1]; x++) {
                    for (int y = miniWindow[2]; y < miniWindow[3]; y ++) {
                        buffer[x][y] = escape(xCoords[x], yCoords[y]);
                        histogram.increment(buffer[x][y]);
                        //System.out.println(xCurr + " " + yCurr + "i");
                        //System.out.println(buffer[x][y] + "\n");
                        //System.out.println(escape_value);
                    }
                    //System.out.println("Line " + xCurr + " \\ " + data.length);
                }
                PARTY_TIME = false;
                
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
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
        //System.out.println(x_curr + ", " + y_curr + "i");
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

    ///Code copied straight from JVM to avoid using Double class
    public static long doubleToLongBits(double value) {
        long result = doubleToRawLongBits(value);
        // Check for NaN based on values of bit fields, maximum
        // exponent and nonzero significand.
        if (((result & DoubleConsts.EXP_BIT_MASK)
                == DoubleConsts.EXP_BIT_MASK)
                && (result & DoubleConsts.SIGNIF_BIT_MASK) != 0L) {
            result = 0x7ff8000000000000L;
        }
        return result;
    }

    public void setMiniWindow(int[] minWindow) {
        this.miniWindow = minWindow;
    }

    public void setBuffer(int[][] buffer) {
        this.buffer = buffer;
    }

    public void setxCoords(double[] xCoords) {
        this.xCoords = xCoords;
    }

    public void setyCoords(double[] yCoords) {
        this.yCoords = yCoords;
    }

    public static int hashCode(double value) {
        long bits = doubleToLongBits(value);
        return (int) (bits ^ (bits >>> 32));
    }

    public void signalRecalculate() {
        this.PARTY_TIME = true;
    }

    public boolean doneCalculating() {
        return this.PARTY_TIME;
    }
}
