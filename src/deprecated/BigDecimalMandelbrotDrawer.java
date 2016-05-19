package deprecated;


import java.awt.Point;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.TreeSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author David
 */
public class BigDecimalMandelbrotDrawer {

    public static int MAX_ITERATIONS = 8192;
    static BigDecimal xEpsilon;
    static BigDecimal yEpsilon;
    static BigDecimal x_curr;
    static BigDecimal y_curr;
    static MathContext mc;

    /**
     * Draws the Mandelbrot set
     *
     * @param width width of the image, in pixels
     * @param height height of the image, in pixels
     * @param window
     * @param data
     * @return
     */
    public static boolean draw(int[][] data, BigDecimalWindow window) {
        long start = System.currentTimeMillis();
        
        mc = new MathContext(window.yRange.scale() * window.yRange.scale() + 10);
        xEpsilon = window.xRange.divide(BigDecimal.valueOf(data.length), mc).multiply(TWO, mc);
        yEpsilon = window.yRange.divide(BigDecimal.valueOf(data[0].length), mc).multiply(TWO, mc);
        x_curr = window.xCenter.subtract(window.xRange, mc);
        y_curr = window.yCenter.subtract(window.yRange, mc);
        for (int xCurr = 0; xCurr < data.length; xCurr++) { //iterating by pixel
            for (int yCurr = 0; yCurr < data[0].length; yCurr++) {
                data[xCurr][yCurr] = escape(x_curr, y_curr, mc);
                // System.out.println(escape_value);
                // System.out.println((255 * escape_value) / MAX_ITERATIONS);
                // System.out.println(escape_value);
                y_curr = y_curr.add(yEpsilon, mc);
            }
            System.out.println("Line " + xCurr + " \\ " + data.length);
            x_curr = x_curr.add(xEpsilon);
            y_curr = window.yCenter.subtract(window.yRange, mc);
        }
        long stop = System.currentTimeMillis();
        System.out.println((stop - start) / 1000.0 + " sec");
        System.out.println("\n\n\n");
        return true;
    }
    static BigDecimal xn;
    static BigDecimal yn;
    static BigDecimal xt;
    static final BigDecimal FOUR = BigDecimal.valueOf(4);
    static final BigDecimal TWO = BigDecimal.valueOf(2);
    static int z;
    static final TreeSet<Integer> hashes = new TreeSet<>();

    private static int escape(BigDecimal x_curr, BigDecimal y_curr, MathContext mc) {
        //System.out.println(x_curr.toPlainString() + ", " + y_curr.toPlainString() + "i");
        xn = x_curr;
        yn = y_curr;
        hashes.clear();
        z = 0;
        while (z < MAX_ITERATIONS - 1) {
            if (xn.multiply(xn, mc).add(yn.multiply(yn, mc), mc).compareTo(FOUR) >= 0) {
                return z;
            }
            if (!hashes.add(37 * xn.hashCode() + yn.hashCode())) {
                //System.out.println("saved " + (MAX_ITERATIONS - z));
                return MAX_ITERATIONS - 1;
            }
            xt = xn.multiply(xn, mc).subtract(yn.multiply(yn, mc), mc).add(x_curr, mc);
            yn = TWO.multiply(xn, mc).multiply(yn, mc).add(y_curr, mc);
            xn = xt;
            z++;
        }

        return MAX_ITERATIONS - 1;
    }

    public static BigDecimal[] coordinateByPoint(Point p, BigDecimalWindow window) {
        return new BigDecimal[]{
            window.xCenter.subtract(window.xRange, mc).add(BigDecimal.valueOf(p.x).multiply(xEpsilon, mc), mc),
            window.yCenter.subtract(window.yRange, mc).add(BigDecimal.valueOf(p.y).multiply(yEpsilon, mc), mc)
        };
    }
}
