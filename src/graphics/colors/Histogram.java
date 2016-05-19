package graphics.colors;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author David
 */
//Thread safe Histogram class
public class Histogram {

    AtomicInteger[] histogram;
    AtomicInteger counter;

    public Histogram(int maxVal) {
        histogram = new AtomicInteger[maxVal + 1];
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = new AtomicInteger(0);
        }
        counter = new AtomicInteger(0);
    }

    public void reset() {
        for (AtomicInteger dataPoint : histogram) {
            dataPoint.set(0);
        }
        counter.set(0);
    }

    public void increment(int point) {
        if (histogram[point].incrementAndGet() == 1) {
            counter.incrementAndGet();
        }
    }

    public int[][] toIntArray() {
        int[][] values = new int[2][counter.get()];
        int vCount = 0;
        for (int i = 0; i < histogram.length; i++) {
            if (histogram[i].get() > 0) {
                values[0][vCount] = i;
                values[1][vCount++] = histogram[i].get();
            }
        }
        return values;

    }
    public String toString(){
        int[][] z  = toIntArray();
        return "\n" + Arrays.toString(z[0]) + "\n" + Arrays.toString(z[1]) + "\n";
    }
    public int[][] getAndReset() {
        int[][] z = toIntArray();
        reset();
        return z;
    }
}
