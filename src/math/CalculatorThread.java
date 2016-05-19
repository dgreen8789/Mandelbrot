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
    private boolean qCheck = true;
    private final TreeSet<Integer> hashes = new TreeSet<>();

    private int escape(double x_curr, double y_curr) {
        //System.out.println(x_curr + ", " + y_curr + "i");
        if (qCheck) {
            //if(p2Check(x_curr, y_curr)) return MAX_ITERATIONS;
        }
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
    
    private BigDecimal bxn;
    private BigDecimal byn;
    private BigDecimal bxt;
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private int escape(BigDecimal x_curr, BigDecimal y_curr, MathContext mc) {
        //System.out.println(x_curr.toPlainString() + ", " + y_curr.toPlainString() + "i");
        bxn = x_curr;
        byn = y_curr;
        hashes.clear();
        z = 0;
        while (z < MAX_ITERATIONS - 1) {
            if (bxn.multiply(bxn, mc).add(byn.multiply(byn, mc), mc).compareTo(FOUR) >= 0) {
                return z;
            }
            if (!hashes.add(37 * bxn.hashCode() + byn.hashCode())) {
                //System.out.println("saved " + (MAX_ITERATIONS - z));
                return MAX_ITERATIONS - 1;
            }
            bxt = bxn.multiply(bxn, mc).subtract(byn.multiply(byn, mc), mc).add(x_curr, mc);
            byn = TWO.multiply(bxn, mc).multiply(byn, mc).add(y_curr, mc);
            bxn = bxt;
            z++;
        }

        return MAX_ITERATIONS - 1;
    }

}
