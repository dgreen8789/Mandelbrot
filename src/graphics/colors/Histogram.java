package graphics.colors;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;


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

    public synchronized void reset() {
        for (AtomicInteger dataPoint : histogram) {
            dataPoint.set(0);
        }
        counter.set(0);
    }

    public synchronized void increment(int point) {
        if (histogram[point].incrementAndGet() == 1) {
            counter.incrementAndGet();
        }
    }

    public synchronized void increment(int point, int amt) {
        if (histogram[point].addAndGet(amt) == amt) {
            counter.incrementAndGet();
        }
    }

    public synchronized void decrement(int point) {
        if (histogram[point].decrementAndGet() < 0) {
            counter.decrementAndGet();
        }
    }

    public synchronized int[][] toIntArray() {
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

    @Override
    public String toString() {
        int[][] z = toIntArray();
        return "\n" + Arrays.toString(z[0]) + "\n" + Arrays.toString(z[1]) + "\n";
    }

    public synchronized int[][] getAndReset() {
        int[][] z = toIntArray();
        reset();
        return z;
    }
}
