/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import java.util.TreeSet;

/**
 *
 * @author David
 */
public abstract interface NumberType {

    public abstract int escape(NumberType x, NumberType y, TreeSet<Integer> hashes, int MAX_ITERATIONS);

    public abstract NumberType add(NumberType addend);

    public abstract NumberType subtract(NumberType subtrahend);

    public abstract NumberType multiply(NumberType multiplicand);

    public abstract NumberType divide(NumberType dividend);

    public abstract NumberType add(double addend);

    public abstract NumberType subtract(double subtrahend);

    public abstract NumberType multiply(double multiplicand);

    public abstract NumberType divide(double dividend);

    public abstract NumberType square();

    public NumberType mult2();

    public String toString();

    public abstract int compareTo(int i);

    @Override
    public abstract int hashCode();

    public abstract NumberType toNextSystem();
}
