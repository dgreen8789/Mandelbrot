package graphics.colors;

import java.util.Arrays;

/**
 *
 * @author David
 */
//Histogram class
public class Histogram {

    private int[] histogram;
    private int counter;

    public Histogram(int maxVal) {
        histogram = new int[maxVal + 1];
    }

    public synchronized void reset() {
        Arrays.fill(histogram, 0);
        counter = 0;
    }

    public synchronized void increment(int point) {
        if (histogram[point] == 0) {
            counter++;
        }
        histogram[point]++;
    }

    public synchronized void decrement(int point) {
        if (histogram[point] == 1) {
            counter--;
        }
        histogram[point]--;
    }

    public synchronized void increment(int point, int amt) {
        if (histogram[point] == 0) {
            counter++;
        }
        histogram[point] += amt;
    }

    public synchronized int[][] toIntArray() {
        //if (counter > 0) {
            int[][] values = new int[2][counter];
            int vCount = 0;
            for (int i = 0; i < histogram.length; i++) {
                if (histogram[i] > 0) {
                    values[0][vCount] = i;
                    values[1][vCount++] = histogram[i];
                }
            }
            return values;
       //}
       // return null;

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

    public int get(int i) {
        return i > 0 ? histogram[i] : -1;
    }
}
