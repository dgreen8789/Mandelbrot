package graphics.base;

import graphics.colors.ColorSchemes;
import math.DoubleWindow;
import math.DoubleMandelbrotCalculator;
import deprecated.BigDecimalMandelbrotDrawer;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.TreeMap;

/**
 *
 * @author David
 */
public class GraphicsController {

    public static final int WINDOW_UPDATE = 0;
    public static final int COLOR_UPDATE = 1;
    private int colorScheme = ColorSchemes.BLACK_AND_WHITE_SQRT;
    private static final int THREAD_COUNT = 4;
    private DoubleWindow window;
    final private int[][] data;
    private TreeMap<Integer, Integer> colors;
    final private BufferedImage img;

    public GraphicsController(int width, int height, Insets insets) {
        width = width - insets.right - insets.left;
        height = height - insets.top - insets.bottom;
        System.out.println(width + ", " + height);
        data = new int[width][height];
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        DoubleMandelbrotCalculator.initialize(THREAD_COUNT, width, height);
    }

    /**
     * Executes the render instructions
     *
     * @param g the graphics context
     * @param width the width of the canvas to draw on
     * @param height the height of the canvas to draw on
     */
    void render(Graphics2D g, boolean[] mustRender) {

        if (mustRender[WINDOW_UPDATE]) {
            DoubleMandelbrotCalculator.getHistogram().reset();
            DoubleMandelbrotCalculator.draw(data, window);

        }
        //System.out.println(DoubleMandelbrotCalculator.getHistogram());
        if (mustRender[WINDOW_UPDATE] || mustRender[COLOR_UPDATE]) {
            colors = ColorSchemes.generate(DoubleMandelbrotCalculator.getHistogram(), colorScheme);
            color(img, data, colors);
        }
        mustRender[COLOR_UPDATE] = mustRender[WINDOW_UPDATE] = false;
        g.drawImage(img, null, 0, 0);

    }

    public void color(BufferedImage image, int[][] mandelbrotData, TreeMap<Integer, Integer> colors) {
        long start = System.currentTimeMillis();
        for (int x = 0; x < mandelbrotData.length; x++) {
            for (int y = 0; y < mandelbrotData[0].length; y++) {
                //System.out.println(x);
                //System.out.println(y);
                //System.out.println(mandelbrotData[x][y]);
                Integer color = colors.get(mandelbrotData[x][y]);
                if (color == null) {
                    //System.out.println("Color not found for point " + "( " + x + ", " + y + " )");
                } else {
                    img.setRGB(x, y, color); //This is a problem, needs optimization, O(N^2) is BAD
                }
            }
        }
        long stop = System.currentTimeMillis();
        System.out.println("Pixel coloring took " + (stop - start) + " ms");
    }

    public void setColorScheme(int colorScheme) {
        this.colorScheme = colorScheme;
    }

    public void setWindow(DoubleWindow window) {
        this.window = window;
    }

    public DoubleWindow getWindow() {
        return window;
    }

}
