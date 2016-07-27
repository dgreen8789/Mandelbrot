/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import static java.lang.Double.doubleToRawLongBits;
import java.util.TreeSet;

/**
 *
 * @author David
 */
public class DoubleNumberType implements NumberType {

    public static final DoubleNumberType ZERO = new DoubleNumberType(0);
    public static final DoubleNumberType ONE = new DoubleNumberType(1);
    public static final DoubleNumberType TEN = new DoubleNumberType(10);

    double u;

    public DoubleNumberType(double u) {
        this.u = u;
    }

    @Override
    public NumberType add(NumberType addend) {
        return new DoubleNumberType(u + ((DoubleNumberType) addend).u);
    }

    @Override
    public NumberType subtract(NumberType subtrahend) {
        return new DoubleNumberType(u - ((DoubleNumberType) subtrahend).u);
    }

    @Override
    public NumberType multiply(NumberType multiplicand) {
        return new DoubleNumberType(u * ((DoubleNumberType) multiplicand).u);
    }

    @Override
    public NumberType divide(NumberType dividend) {
        return new DoubleNumberType(u / ((DoubleNumberType) dividend).u);
    }

    @Override
    public NumberType add(double addend) {
        return new DoubleNumberType(u + addend);
    }

    @Override
    public NumberType subtract(double subtrahend) {
        return new DoubleNumberType(u - subtrahend);
    }

    @Override
    public NumberType multiply(double multiplicand) {
        return new DoubleNumberType(u * multiplicand);
    }

    @Override
    public NumberType divide(double dividend) {
        return new DoubleNumberType(u / dividend);
    }

    @Override
    public NumberType square() {
        return new DoubleNumberType(u * u);
    }

    @Override
    public int compareTo(int i) {
        return Double.compare(u, i);

    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final DoubleNumberType other = (DoubleNumberType) obj;
        if (Double.doubleToLongBits(this.u) != Double.doubleToLongBits(other.u)) {
            return false;
        }
        return true;
    }

    @Override
    public NumberType mult2() {
        return new DoubleNumberType(2 * u);
    }

    public static int hashCode(double value) {
        long bits = doubleToRawLongBits(value);
        return (int) (bits ^ (bits >>> 32));
    }

    public int escape(NumberType x_curr, NumberType y_curr, TreeSet<Integer> hashes, int MAX_ITERATIONS) {
        double xn, yn, y0, x0;
        x0 = xn = ((DoubleNumberType) x_curr).u;
        yn = y0 = ((DoubleNumberType) y_curr).u;
        double xt = 0;
        hashes.clear();
        int z = 0;
        while (z < MAX_ITERATIONS - 1) {
            if (xn * xn + yn * yn > 4) {
                //System.out.println("problem");
                return z;
            }
            if (!hashes.add(37 * hashCode(xn) + hashCode(yn))) {
                //System.out.println("saved " + (MAX_ITERATIONS - z));
                return MAX_ITERATIONS;
            }
            xt = xn * xn - yn * yn + x0;
            yn = 2 * xn * yn + y0;
            xn = xt;
            z++;
        }
        return MAX_ITERATIONS;
    }

    @Override
    public NumberType toNextSystem() {
        return new DoubleDoubleNumberType(u);
    }
    
    public String toString() {
        return "" + u;
    }

}
