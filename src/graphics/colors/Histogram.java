package graphics.colors;

import java.util.Arrays;

/**
 *
 * @author David
 */
//Histogram class
public class Histogram {

    int[] histogram;
    int counter;

    public Histogram(int maxVal) {
        histogram = new int[maxVal + 1];
    }

    public void reset() {
        Arrays.fill(histogram, 0);
        counter = 0;
    }

    public void increment(int point) {
        if (point >= 0) {
            if (histogram[point] == 0) {
                counter++;
            }
            histogram[point]++;
        }
    }

    public void increment(int point, int amt) {
        if (histogram[point] == 0) {
            counter++;
        }
        histogram[point] += amt;
    }

    public int[][] toIntArray() {
        int[][] values = new int[2][counter];
        int vCount = 0;
        for (int i = 0; i < histogram.length; i++) {
            if (histogram[i] > 0) {
                values[0][vCount] = i;
                values[1][vCount++] = histogram[i];
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
