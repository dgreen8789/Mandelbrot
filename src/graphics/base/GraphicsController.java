package graphics.base;

import graphics.colors.ColorSchemes;
import math.DoubleWindow;
import math.DoubleMandelbrotCalculator;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.TreeMap;

/**
 *
 * @author David
 */
public class GraphicsController {

    public static final int WINDOW_ZOOM_UPDATE = 0;
    public static final int WINDOW_PAN_UP_UPDATE = 1;
    public static final int WINDOW_PAN_DOWN_UPDATE = 2;
    public static final int WINDOW_PAN_LEFT_UPDATE = 3;
    public static final int WINDOW_PAN_RIGHT_UPDATE = 4;
    public static final int WINDOW_COLOR_UPDATE = 5;
    public static final int ANYTHING = 6;

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
        DoubleMandelbrotCalculator.initialize(THREAD_COUNT, width, height, data);
    }

    /**
     * Executes the render instructions
     *
     * @param g the graphics context
     * @param width the width of the canvas to draw on
     * @param height the height of the canvas to draw on
     */
    private int pixelShiftDistance = 10;

    void render(Graphics2D g, boolean[] mustRender) {
        if (mustRender[ANYTHING]) {
            if (mustRender[WINDOW_ZOOM_UPDATE]) {
                DoubleMandelbrotCalculator.getHistogram().reset();
                DoubleMandelbrotCalculator.draw(window);
            }
            if (mustRender[WINDOW_PAN_RIGHT_UPDATE]) {
                DoubleMandelbrotCalculator.panRight(pixelShiftDistance, window);
            }
            if (mustRender[WINDOW_PAN_LEFT_UPDATE]) {
                DoubleMandelbrotCalculator.panLeft(pixelShiftDistance, window);
            }
            if (mustRender[WINDOW_PAN_DOWN_UPDATE]) {
                DoubleMandelbrotCalculator.panDown(pixelShiftDistance, window);
            }
             if (mustRender[WINDOW_PAN_UP_UPDATE]) {
                DoubleMandelbrotCalculator.panUp(pixelShiftDistance, window);
            }
             if(mustRender[WINDOW_COLOR_UPDATE]){
                 colorScheme = ColorSchemes.getNextScheme(colorScheme);
             }
            colors = ColorSchemes.generate(DoubleMandelbrotCalculator.getHistogram(), colorScheme);
            color(img, data, colors);
            Arrays.fill(mustRender, false);
        }
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
                    img.setRGB(x, y, color);
                    //This is a problem, needs optimization, O(N^2) is BAD
                    //jk its like .1% of processor time lol
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

    public int getPixelShiftDistance() {
        return pixelShiftDistance;
    }

    public void setPixelShiftDistance(int pixelShiftDistance) {
        this.pixelShiftDistance = pixelShiftDistance;
    }

}
