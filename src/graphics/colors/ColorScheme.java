package graphics.colors;

import java.awt.Color;
import java.util.TreeMap;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 *
 * @author David
 */
public enum ColorScheme {

    BLACK_AND_WHITE,
    FIRE,
    BLUE;
    private static final TreeMap<Integer, Integer> colors = new TreeMap<>();

    /**
     *
     * @param histogram a histogram of the escape values generated by the
     * mandelbrot calculator
     * @param scheme the scheme
     * @return An TreeMap of colors generated with the selected scheme in sRGB,
     * where the key of the escape value maps to the appropriate color space;
     *
     * Does nothing on invalid scheme code
     */
    public static TreeMap<Integer, Integer> generate(Histogram histogram, ColorScheme scheme) {
        long start = System.currentTimeMillis();
        colors.clear();
        switch (scheme) {
            case BLACK_AND_WHITE:
                bw(histogram.toIntArray());
                break;
            case FIRE:
                fire(histogram.toIntArray());
                break;
            case BLUE:
                blue(histogram.toIntArray());
                break;
        }
        long stop = System.currentTimeMillis();
        System.out.println("Color generation took " + (stop - start) + " ms");
        return colors;
    }

    public static void bw(int[][] data) {
        double len = data[1].length;
        IntStream.range(0, data[1].length).forEach(x -> colors.put(data[0][x],
                rgbaToColorCode(255, 255, 255, (int) (255 * Math.sqrt(x / len)))));
    }

    public static void fire(int[][] data) {
        double len = data[1].length;
        IntStream.range(0, data[1].length).asDoubleStream().forEach(x -> colors.put(data[0][(int) x], rgbaToColorCode(
                255,
                (int) (255 * Math.pow(x / len, 1 / Math.E - 1 / (10 * Math.PI))),
                0,
                (int) (20 + 230 * (Math.cbrt(x / len))))
        ));
        colors.put(colors.lastKey(), 0);
    }
    private static void blue(int[][] data) {
        double len = data[1].length;
        double constant = Math.pow(Math.E / Math.PI, 9);
        IntStream.range(0, data[1].length).asDoubleStream().forEach(x -> colors.put(data[0][(int) x], rgbaToColorCode(
                (int) (255 * Math.pow(x / len, constant)),
                0,
                255,
                (int) (20 + 230 * (Math.cbrt(x / len))))
        ));
        colors.put(colors.lastKey(), 0);
    }

    private static int rgbaToColorCode(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24)
                | ((r & 0xFF) << 16)
                | ((g & 0xFF) << 8)
                | ((b & 0xFF));
    }

}
