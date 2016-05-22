package graphics.colors;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.TreeMap;
import java.util.stream.IntStream;


/**
 *
 * @author David
 */
public class ColorSchemes {
    
    public static final int BLACK_AND_WHITE_SQRT = 1;
    
    private static final Method[] methods = ColorSchemes.class.getMethods();
    private static final TreeMap<Integer, Integer> colors = new TreeMap<>();

    /**
     *
     * @param histogram a histogram of the escape values generated by the
     * mandelbrot calculator
     * @param scheme the scheme
     * @return An TreeMap of colors generated with the selected scheme in sRGB,
     * where the key of the escape value maps to the appropriate color space;
     */
    public static TreeMap<Integer, Integer> generate(Histogram histogram, int scheme) {
           return bw(histogram);
        

    }
    
    public static TreeMap<Integer, Integer> bw(Histogram histogram) {
        long start = System.currentTimeMillis();
        colors.clear();
        int[][] data = histogram.toIntArray();
        //System.out.println(data[1].length);
        double len = data[1].length;
        
        int[] values = IntStream.range(0, data[1].length)
                //Avoid instantiating a color, at some point
                .map(x -> new Color(1.0f, 1.0f, 1.0f, (float) Math.sqrt(x / len))
                        .getRGB()).toArray();
        //System.out.println(Arrays.toString(values));
        for (int i = 0; i < values.length; i++) {
            colors.put(data[0][i], values[i]);
        }
        //System.out.println(colors);
        long stop = System.currentTimeMillis();
        System.out.println("Color generation took " + (stop - start) + " ms");
        return colors;
    }

    public static int getNextScheme(int colorScheme) {
        
        return colorScheme;
    }
    
}
