/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unused;

import java.util.HashSet;
import math.numbertypes.DoubleDouble;
import math.numbertypes.NumberType;

/**
 *
 * @author David
 */
public strictfp class QuadDouble implements NumberType, Cloneable {

    private double a0, a1, a2, a3, r0, r1, r2;

    public static final int MAX_ZOOM = 200;

    public static final QuadDouble ZERO = new QuadDouble(0, 0, 0, 0);
    public static final QuadDouble ONE = new QuadDouble(1.0, 0, 0, 0);
    public static final QuadDouble TEN = new QuadDouble(10.0, 0, 0, 0);

    public QuadDouble(double a, double b, double c, double d) {
        this.a0 = a;
        this.a1 = b;
        this.a2 = c;
        this.a3 = d;
        renormalize(a0, a1, a2, a3, 0);
    }

    private QuadDouble(double a, double b, double c, double d, Object marker) {
        this.a0 = a;
        this.a1 = b;
        this.a2 = c;
        this.a3 = d;
    }

    public QuadDouble(double a0) {
        this.a0 = a0;
        a1 = a2 = a3 = 0;
    }

    //Helper methods
    private void quickTwoSum(double a, double b) {
        r0 = a + b;
        r1 = (b - (r0 - a));
    }

    private void twoSum(double a, double b) {
        r0 = a + b;
        double v = r0 - a;
        r1 = (a - (r0 - v)) + (b - v);
//        System.out.println("TwoSum");
//        System.out.printf("%.7f + %.7f ----> (%.7f, %.7f)\n", a, b, r0, r1);
//        System.out.println("");
    }

    public void fourSum(double a0, double a1, double b0, double b1) {
        double a, b, c, d, e, f;
        e = a0 + b0;
        d = a0 - e;
        a = a1 + b1;
        f = a1 - a;
        d = ((a0 - (d + e)) + (d + b0)) + a;
        b = e + d;
        c = ((a1 - (f + a)) + (f + b1)) + (d + (e - b));
        r0 = b + c;
        r1 = c + (b - a);
    }

    private void split(double a) {
        double t = 0x08000001 * a;
        r0 = (t - (t - a));
        r1 = a - r0;
    }

    //FMA method not included because it doesn't appear java supports it
    private void twoProd(double a, double b) {
        double p, ahi, alo, bhi, blo;
        p = a * b;
        split(a);
        ahi = r0;
        alo = r1;
        split(b);
        bhi = r0;
        blo = r1;
        r0 = p;
        r1 = ((ahi * bhi - p) + ahi * blo + alo * bhi) + alo * blo;

    }

    private QuadDouble renormalize(double a0, double a1, double a2, double a3, double a4) {
        //System.out.printf("(%.3f, %.3f, %.3f, %.3f, %.3f) ----> ", a0, a1, a2, a3, a4);
        // System.out.printf("(%s, %s, %s, %s, %s) ----> ", a0, a1, a2, a3, a4);
        double s, e, t0, t1, t2, t3, t4, a0r, a1r, a2r, a3r, ulp;
        int k;
        double[] b_arr = new double[4];
        a0r = a1r = a2r = a3r = 0;
        if (a0 == 0) {

            a0r = a0 = 1.0;
        }
        ulp = Math.ulp(a0);
        if (a1 == 0) {
            a1r = a1 = ulp / 2;
        } else if (a1 > ulp) {
            s = (a1 / ulp) * ulp;
            a0 += s;
            a1 -= s;
        }
        ulp = Math.ulp(a1);
        if (a2 == 0) {
            a2r = a2 = ulp / 2;
        } else if (a2 > ulp) {
            s = (a2 / ulp) * ulp;
            a1 += s;
            a2 -= s;
        }
        ulp = Math.ulp(a2);
        if (a3 == 0) {
            a3r = a3 = ulp / 2;
        } else if (a3 > ulp) {
            s = (a3 / ulp) * ulp;
            a2 += s;
            a3 -= s;
        }
        ulp = Math.ulp(a3);
        if (a4 > ulp) {
            s = (a4 / ulp) * ulp;
            a3 += s;
            a4 -= s;
        }

        quickTwoSum(a3, a4);
        s = r0;
        t4 = r1;
        quickTwoSum(a2, s);
        s = r0;
        t3 = r1;
        quickTwoSum(a1, s);
        s = r0;
        t2 = r1;
        quickTwoSum(a0, s);
        s = t0 = r0;
        t1 = r1;
        k = 0;

        //begin unrolled for loop
        quickTwoSum(s, t1);
        s = r0;
        e = r1;
        if (e != 0) {
            b_arr[k] = s;
            s = e;
            k++;
        }

        quickTwoSum(s, t2);
        s = r0;
        e = r1;
        if (e != 0) {
            b_arr[k] = s;
            s = e;
            k++;
        }

        quickTwoSum(s, t3);
        s = r0;
        e = r1;
        if (e != 0) {
            b_arr[k] = s;
            s = e;
            k++;
        }

        quickTwoSum(s, t4);
        s = r0;
        e = r1;
        if (e != 0) {
            b_arr[k] = s;
            s = e;
            k++;
        }

        b_arr[0] -= a0r;
        b_arr[1] -= a1r;
        b_arr[2] -= a2r;
        b_arr[3] -= a3r;
//        System.out.println("Renomalize");
        //System.out.printf("%.3f, %.3f, %.3f, %.3f)\n",
        //System.out.printf("%s, %s, %s, %s)\n",
        //        b_arr[0], b_arr[1], b_arr[2], b_arr[3]);
//        System.out.println("");
        QuadDouble b = new QuadDouble(b_arr[0], b_arr[1], b_arr[2], b_arr[3], null);
        return b;
        //return new QuadDouble(b_arr[0] - a0r, b_arr[1] - a1r, b_arr[2] - a2r, b_arr[3] - a3r);

    }

    private void negSelf() {
        a0 = -a0;
        a1 = -a1;
        a2 = -a2;
        a3 = -a3;
    }

    public QuadDouble negation() {
        return new QuadDouble(-a0, -a1, -a2, -a3, null);
    }

    private void threeSumHelper(double x, double y, double z) {
        twoSum(x, y);
        r2 = r1;
        twoSum(r0, z);
    }

    private void threeSumOne(double x, double y, double z) {
        threeSumHelper(x, y, z);
        double temp;
        temp = r0;
        twoSum(r2, r1);
        r2 = r1;
        r1 = r0;
        r0 = temp;

    }

    private void threeSumTwo(double x, double y, double z) {
        threeSumHelper(x, y, z);
        r1 = r2 + r1;
    }

    private void threeSumThree(double x, double y, double z) {
        r0 = (x + y) + z;
    }

    private void sixThreeSum(double a, double b, double c, double d, double e, double f) {
        double temp1, temp2, temp3, temp4, temp5;
        threeSumOne(a, b, c);
        temp1 = r0;
        temp2 = r1;
        temp3 = r2;
        threeSumOne(d, e, f);
        temp4 = r0;
        temp5 = r1;
        temp3 = temp3 + r2;
        twoSum(temp1, temp4);
        temp1 = r0;
        r2 = r1;
        twoSum(temp2, temp5);
        temp2 = r1;
        twoSum(r0, r2);
        r2 = r1 + r0 + temp3;
        r1 = r0;
        r0 = temp1;

    }

    private void nineTwoSum(double a, double b, double c, double d, double e, double f, double g, double h, double i) {
        double temp1, temp2, temp3, temp4;
        twoSum(c, d);
        temp1 = r0;
        temp2 = r1;
        twoSum(b, e);
        fourSum(temp1, temp2, r0, r1);
        temp1 = r0;
        temp2 = r1;
        twoSum(g, h);
        temp3 = r0;
        temp4 = r1;
        twoSum(f, i);
        fourSum(temp3, temp4, r0, r1);
        fourSum(r0, r1, temp1, temp2);
        threeSumTwo(a, r0, r1);

    }

    @Override
    public QuadDouble add(NumberType addend) {
        double c0, c1, c2, c3, c4, c5, c6;
        QuadDouble o = (QuadDouble) addend;
        twoSum(a0, o.a0);
        c0 = r0;
        c1 = r1;
        twoSum(a1, o.a1);
        c3 = r1;
        twoSum(r0, c1);
        c1 = r0;
        c2 = r1;
        twoSum(a2, o.a2);
        c4 = r1;
        threeSumOne(r0, c3, c2);
        c2 = r0;
        c5 = r2;
        r2 = r1;
        twoSum(a3, o.a3);
        c6 = r1;
        threeSumTwo(r0, c4, r2);
        c3 = r0;
        threeSumThree(c6, r1, c5);
        return renormalize(c0, c1, c2, c3, r0);
    }

    @Override
    public QuadDouble subtract(NumberType subtrahend) {
        QuadDouble o = (QuadDouble) subtrahend;
        return add(o.negation());
    }

    @Override
    public QuadDouble multiply(NumberType multiplicand) {
        QuadDouble o = (QuadDouble) multiplicand;
        double a0b0hi, //O(1)
                a0b0lo, a0b1hi, a1b0hi, //O(E)
                a1b0lo, a0b1lo, a1b1hi, a2b0hi, a0b2hi, //O(E^2)
                a1b1lo, a2b0lo, a0b2lo, a0b3hi, a1b2hi, a2b1hi, a3b0hi, //O(E^3)
                a0b3lo, a1b2lo, a2b1lo, a3b0lo, a1b3, a2b2, a3b1; // O(E^4) 
        double c0, c1, c2, c3, c4, temp1, temp2;
        //unrolled for loop, autogenerated by code below
////          for (int i = 0; i <= 3; i++) {
////            for (int j = 0; i + j <= 3 ; j++) {
////                System.out.printf("twoProd(a%d, o.a%d);\n",i,j);
////                System.out.printf("a%db%dhi = r0;\n",i,j);
////                System.out.printf("a%db%dlo = r1;\n\n", i, j);
////            }
////        }
        twoProd(a0, o.a0);
        a0b0hi = r0;
        a0b0lo = r1;

        twoProd(a0, o.a1);
        a0b1hi = r0;
        a0b1lo = r1;

        twoProd(a0, o.a2);
        a0b2hi = r0;
        a0b2lo = r1;

        twoProd(a0, o.a3);
        a0b3hi = r0;
        a0b3lo = r1;

        twoProd(a1, o.a0);
        a1b0hi = r0;
        a1b0lo = r1;

        twoProd(a1, o.a1);
        a1b1hi = r0;
        a1b1lo = r1;

        twoProd(a1, o.a2);
        a1b2hi = r0;
        a1b2lo = r1;

        twoProd(a2, o.a0);
        a2b0hi = r0;
        a2b0lo = r1;

        twoProd(a2, o.a1);
        a2b1hi = r0;
        a2b1lo = r1;

        twoProd(a3, o.a0);
        a3b0hi = r0;
        a3b0lo = r1;

        a1b3 = a1 * o.a3;
        a2b2 = a2 * o.a2;
        a3b1 = a3 * o.a1;

        c0 = a0b0hi;

        threeSumOne(a0b0lo, a0b1hi, a1b0hi);

        c1 = r0;
        temp1 = r2;

        sixThreeSum(r1, a0b1lo, a1b0lo, a2b0hi, a1b1hi, a0b2hi);
        c2 = r0;
        temp2 = r2;

        nineTwoSum(temp1, r1, a2b0lo, a1b1lo, a0b2lo, a0b3hi, a1b2hi, a2b1hi, a3b0hi);
        c3 = r0;

        c4 = r1 + temp2 + a1b2lo + a2b1lo + a0b3lo + a3b0lo + a1b3 + a2b2 + a3b1; //nineOneSum       
        return renormalize(c0, c1, c2, c3, c4);
    }

    @Override
    public QuadDouble divide(NumberType dividend) {
        QuadDouble o = (QuadDouble) dividend;
        double q0, q1, q2, q3, q4;
        QuadDouble r;
        q0 = a0 / o.a0;
        r = this.subtract(dividend.multiply(q0));
        q1 = r.a0 / o.a0;
        r = r.subtract(dividend.multiply(q1));
        q2 = r.a0 / o.a0;
        r = r.subtract(dividend.multiply(q2));
        q3 = r.a0 / o.a0;
        r = r.subtract(dividend.multiply(q3));
        q4 = r.a0 / o.a0;
        return renormalize(q0, q1, q2, q3, q4);
    }

    @Override
    public QuadDouble add(double addend) {
        double c0, c1, c2;
        twoSum(a0, addend);
        c0 = r0;
        twoSum(a1, r1);
        c1 = r0;
        twoSum(a2, r1);
        c2 = r0;
        twoSum(a3, r1);
        return renormalize(c0, c1, c2, r0, r1);
    }

    @Override
    public QuadDouble subtract(double subtrahend) {
        return add(-subtrahend);
    }

    @Override
    public QuadDouble multiply(double m) {
        double c0, c1, c2, c3, c4;
        twoProd(a0, m);
        c0 = r0;
        c1 = r1;
        twoProd(a1, m);
        c2 = r1;
        twoSum(r0, c1);
        c1 = r0;
        c4 = r1;
        twoProd(a2, m);
        c3 = r1;
        threeSumOne(r0, c2, c4);
        c2 = r0;
        threeSumTwo(a3 * m, c3, r1);
        return renormalize(c0, c1, c2, r0, r1 + r2);
    }

    @Override
    public QuadDouble divide(double dividend) {
        return multiply(1 / dividend);
    }

    @Override
    public QuadDouble square() {
        double a0sqhi, //O(E^0)
                a0sqlo, a01hi, //O(E^1)
                a02hi, a01lo, a1sqhi, //O(E^2)
                a1sqlo, a02lo, a03hi, a12hi, //O(E^3)
                a03lo, a12lo, a1b3, a2sq; // O(E^4) 
        double c0, c1, c2, c3, c4, temp1, temp2;

        twoProd(a0, a0);
        a0sqhi = r0;
        a0sqlo = r1;

        twoProd(a0, a1);
        a01hi = r0;
        a01lo = r1;

        twoProd(a0, a2);
        a02hi = r0;
        a02lo = r1;

        twoProd(a0, a3);
        a03hi = r0;
        a03lo = r1;

        twoProd(a1, a1);
        a1sqhi = r0;
        a1sqlo = r1;

        twoProd(a1, a2);
        a12hi = r0;
        a12lo = r1;

        a1b3 = 2 * a1 * a3;
        a2sq = a2 * a2;

        c0 = a0sqhi;

        threeSumOne(a0sqlo, a01hi, a01hi);

        c1 = r0;
        temp1 = r2;

        sixThreeSum(r1, a01lo, a01lo, a02hi, a1sqhi, a02hi);
        c2 = r0;
        temp2 = r2;

        nineTwoSum(temp1, r1, a02lo, a1sqlo, a02lo, a03hi, a12hi, a12hi, a03hi);
        c3 = r0;

        c4 = r1 + temp2 + a12lo + a12lo + a03lo + a03lo + 2 * a1b3 + a2sq; //nineOneSum       
        return renormalize(c0, c1, c2, c3, c4);
    }

    public String toString() {

        return "( " + a0 + " + " + a1 + " + " + a2 + " + " + a3 + ")\n" + (a0 + a1 + a2 + a3);
    }

    @Override
    public NumberType mult2() {
        return new QuadDouble(a0 * 2, a1 * 2, a2 * 2, a3 * 2, null);
    }

    public int mEscape(NumberType x, NumberType y, HashSet<Integer> hashes, int MAX_ITERATIONS) {
        QuadDouble xn;
        QuadDouble yn, x0, y0, xsq, ysq;
        xn = x0 = (QuadDouble) x;
        yn = y0 = (QuadDouble) y;
        //y0 = y0.multiply(1);
        //x0 = x0.multiply(1);
        int z = 0;
        while (z < MAX_ITERATIONS - 1) {
            xsq = xn.square();
            ysq = yn.square();
            if (xsq.add(ysq).longValue() > 4) {
                //System.out.println("problem");
                return z;
            }
            if (!hashes.add(octupleHash(xn, yn))) {
                //System.out.println("saved " + (MAX_ITERATIONS - z));
                return MAX_ITERATIONS;
            }

            yn = (QuadDouble) xn.multiply(yn).mult2().add(y0);
            xn = xsq.subtract(ysq).add(x0);
            z++;
        }
        return MAX_ITERATIONS;
    }

    @Override
    public int compareTo(int i) {
        return a0 > i ? 1 : 0;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new QuadDouble(a0, a1, a2, a3, null);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.a0) ^ (Double.doubleToLongBits(this.a0) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.a1) ^ (Double.doubleToLongBits(this.a1) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.a2) ^ (Double.doubleToLongBits(this.a2) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.a3) ^ (Double.doubleToLongBits(this.a3) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QuadDouble other = (QuadDouble) obj;
        if (Double.doubleToLongBits(this.a0) != Double.doubleToLongBits(other.a0)) {
            return false;
        }
        if (Double.doubleToLongBits(this.a1) != Double.doubleToLongBits(other.a1)) {
            return false;
        }
        if (Double.doubleToLongBits(this.a2) != Double.doubleToLongBits(other.a2)) {
            return false;
        }
        if (Double.doubleToLongBits(this.a3) != Double.doubleToLongBits(other.a3)) {
            return false;
        }
        return true;
    }

    @Override
    public NumberType toNextSystem() {
        throw new UnsupportedOperationException("NOPE");
    }

    @Override
    public NumberType toPreviousSystem() {
        return new DoubleDouble(a0, a1);
    }

    public int octupleHash(QuadDouble xn, QuadDouble yn) {
        int hash = 5;
        hash = 37 * hash + (int) (Double.doubleToLongBits(xn.a0) ^ (Double.doubleToLongBits(xn.a0) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(xn.a1) ^ (Double.doubleToLongBits(xn.a1) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(xn.a2) ^ (Double.doubleToLongBits(xn.a2) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(xn.a3) ^ (Double.doubleToLongBits(xn.a3) >>> 32));

        hash = 37 * hash + (int) (Double.doubleToLongBits(yn.a0) ^ (Double.doubleToLongBits(yn.a0) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(yn.a1) ^ (Double.doubleToLongBits(yn.a1) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(yn.a2) ^ (Double.doubleToLongBits(yn.a2) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(yn.a3) ^ (Double.doubleToLongBits(yn.a3) >>> 32));
        return hash;
    }

    private long longValue() {
        return (long) a0;
    }

    private String debugString() {
        String str = "";
        str += "Value: " + a0 + "\tulp: " + Math.ulp(a0) + "   (2^" + Math.log(Math.ulp(a0)) / Math.log(2) + ")";
        str += "Value: " + a1 + "\tulp: " + Math.ulp(a1) + "   (2^" + Math.log(Math.ulp(a1)) / Math.log(2) + ")";
        str += "Value: " + a2 + "\tulp: " + Math.ulp(a2) + "   (2^" + Math.log(Math.ulp(a2)) / Math.log(2) + ")";
        str += "Value: " + a3 + "\tulp: " + Math.ulp(a3) + "   (2^" + Math.log(Math.ulp(a3)) / Math.log(2) + ")";
        return str;
    }

    @Override
    public int JEscape(NumberType x0, NumberType y0, NumberType c0, NumberType c1, HashSet<Integer> hashes, int MAX_ITERATIONS) {
        return -1;
    }

}
