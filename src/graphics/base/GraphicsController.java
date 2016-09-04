package graphics.base;

import graphics.colors.ColorScheme;
import java.awt.Color;
import math.MandelbrotCalculator;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;
import math.MRectangle;

/**
 *
 * @author David
 */
public class GraphicsController {

    public enum GraphicsOperation {
        WINDOW_ZOOM_IN_UPDATE, WINDOW_ZOOM_OUT_UPDATE, WINDOW_PAN_UP_UPDATE, WINDOW_PAN_DOWN_UPDATE,
        WINDOW_PAN_LEFT_UPDATE, WINDOW_PAN_RIGHT_UPDATE, WINDOW_COLOR_UPDATE, REFRESH, SUPER_SAMPLE_TOGGLE,
        SHOW_BOXES
    }

    public static final int ANYTHING = 7;
    public static final int MAX_SHIFT_DISTANCE = Integer.MAX_VALUE;
    
    private static final double CONST_PAN_COEFF = 5;
    private static final double LINEAR_PAN_COEFF = 1;
    private static final double QUADRATIC_PAN_COEFF  = 0;
            
    public static MandelbrotCalculator calculator;
    public static final int THREAD_COUNT = 4;

    private final ColorScheme[] schemes;
    private int colorScheme;
    private TreeMap<Integer, Integer> colors;
    final private BufferedImage img;
    private final int width;
    private final int height;
    private static final boolean SAVE_IMAGES_TO_FILE = true;
    private static final String IMAGE_PATH = "Z:\\Mandelbrot Image Logs";

    private InputHandler inputHandler;
    private final ImageWriter writer;

    public GraphicsController(int w, int h, Insets insets) {
        this.width = w - insets.right - insets.left;
        this.height = h - insets.top - insets.bottom;
        System.out.println(width + ", " + height);
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        calculator = new MandelbrotCalculator(width, height, 0);
        schemes = ColorScheme.values();
        writer = SAVE_IMAGES_TO_FILE ? new ImageWriter(IMAGE_PATH, width, height) : null;

    }
    private int determineShiftDistance(int consecutiveShifts){
        return (int)Math.min(CONST_PAN_COEFF + LINEAR_PAN_COEFF * consecutiveShifts + 
                consecutiveShifts * consecutiveShifts * QUADRATIC_PAN_COEFF, MAX_SHIFT_DISTANCE);
    }
    /**
     * Executes the render instructions
     *
     * @param g the graphics context
     * @param width the width of the canvas to zoom on
     * @param height the height of the canvas to zoom on
     */
    private int numShifts = 1;

    private GraphicsOperation lastCommand;
    int[][] data; //Should only be used as a pointer, not for any operations.

    void render(Graphics2D g, ArrayList<GraphicsOperation> input) {

        if (!input.isEmpty()) {
            if (input.contains(GraphicsOperation.WINDOW_ZOOM_IN_UPDATE)) {
                calculator.getHistogram().reset();
                calculator.zoom(true, inputHandler.getMousePoint());
                lastCommand = GraphicsOperation.WINDOW_ZOOM_IN_UPDATE;
            }
            if (input.contains(GraphicsOperation.WINDOW_ZOOM_OUT_UPDATE)) {
                calculator.getHistogram().reset();
                calculator.zoom(false, inputHandler.getMousePoint());
                lastCommand = GraphicsOperation.WINDOW_ZOOM_OUT_UPDATE;
            }

            if (input.contains(GraphicsOperation.WINDOW_PAN_RIGHT_UPDATE)) {
                                calculator.getHistogram().reset();

                if (lastCommand == GraphicsOperation.WINDOW_PAN_RIGHT_UPDATE) {
                    numShifts ++;
                } else {
                    numShifts = 1;
                }
                lastCommand = GraphicsOperation.WINDOW_PAN_RIGHT_UPDATE;
                calculator.panRight(determineShiftDistance(numShifts));
            }
            if (input.contains(GraphicsOperation.WINDOW_PAN_LEFT_UPDATE)) {
                if (lastCommand == GraphicsOperation.WINDOW_PAN_LEFT_UPDATE) {
                    numShifts ++;
                } else {
                    numShifts = 1;
                }
                lastCommand = GraphicsOperation.WINDOW_PAN_LEFT_UPDATE;
                calculator.panLeft(determineShiftDistance(numShifts));
            }
            if (input.contains(GraphicsOperation.WINDOW_PAN_DOWN_UPDATE)) {
                if (lastCommand == GraphicsOperation.WINDOW_PAN_DOWN_UPDATE) {
                    numShifts ++;
                } else {
                    numShifts = 1;
                }
                lastCommand = GraphicsOperation.WINDOW_PAN_DOWN_UPDATE;
                calculator.panDown(determineShiftDistance(numShifts));
            }
            if (input.contains(GraphicsOperation.WINDOW_PAN_UP_UPDATE)) {
                if (lastCommand == GraphicsOperation.WINDOW_PAN_UP_UPDATE) {
                    numShifts ++;
                } else {
                    numShifts = 1;
                }
                lastCommand = GraphicsOperation.WINDOW_PAN_UP_UPDATE;
                calculator.panUp(determineShiftDistance(numShifts));
            }
            if (input.contains(GraphicsOperation.WINDOW_COLOR_UPDATE)) {
                lastCommand = GraphicsOperation.WINDOW_COLOR_UPDATE;
                colorScheme = ++colorScheme % schemes.length;
            }
            if ((input.contains(GraphicsOperation.REFRESH))) {
                calculator.draw();
                lastCommand = GraphicsOperation.REFRESH;
            }

            if ((input.contains(GraphicsOperation.SUPER_SAMPLE_TOGGLE))) {
                //calculator.getHistogram().reset();
                superSample = !superSample;

            }
            if ((input.contains(GraphicsOperation.SHOW_BOXES))) {
                drawBoxes = !drawBoxes;

            }
            //debug code
//            int[][]  d1 = calculator.getDataArray();
//            int[][]  d2 = calculator.getUnSampledDataArray();
//            for (int i = 0; i < d2.length; i++) {
//                for (int j = 0; j < d2[0].length; j++) {
//                    d1[i][j] -= d2[i][j];
//                }
//            }
//            for (int[] is : d2) {
//                System.out.println(Arrays.toString(is));
//            }
            data = calculator.getDataArray();
            colors = ColorScheme.generate(calculator.getHistogram(), schemes[colorScheme]);
            color(img, data, colors);

            if (writer != null) {
                writer.writeToFile(img, calculator.getWindow());
            }
        }
        input.clear();
        g.setColor(Color.red);
        String str = calculator.getWindow().toPresentationString();
        g.drawImage(img, null, 0, 0);
        int textLength = Math.min(width, str.length() / 75 * width);
        g.setFont(GraphicsUtilities.fillRect(str, g, textLength, MAX_TEXT_HEIGHT));
        g.drawString(str, 0, height - MAX_TEXT_HEIGHT / 2);
        if (superSample) {
            str = MandelbrotCalculator.SUPER_SAMPLING_FACTOR * MandelbrotCalculator.SUPER_SAMPLING_FACTOR + "x";
            g.setFont(GraphicsUtilities.fillRect(str, g, width / 10, MAX_TEXT_HEIGHT));
            g.drawString(str, 50, 50);
        }
        if (drawBoxes) {
            for (MRectangle r : calculator.getBoxes()) {
                if (r == null) {
                } else //System.out.println(r);
                if (r.isFilled()) {
                    g.setColor(Color.MAGENTA);
                    g.fill(r);
                } else {
                    g.setColor(Color.BLUE);
                    g.draw(r);
                }
            }
        }

    }
    private boolean drawBoxes = false;
    private boolean superSample = false;

    private static final int MAX_TEXT_HEIGHT = 40;

    public void color(BufferedImage image, int[][] mandelbrotData, TreeMap<Integer, Integer> colors) {
        long start = System.currentTimeMillis();
        for (int x = 0; x < mandelbrotData.length; x++) {
            for (int y = 0; y < mandelbrotData[0].length; y++) {

                Integer color = colors.get(mandelbrotData[x][y]);
                //colors.replace(colors.lastKey(), Color.RED.getRGB()); //makes the most expensive renders be red
                if (color == null) {
                    img.setRGB(x, y, Color.RED.getRGB());    //comment block for box tracing
                } else {
                    img.setRGB(x, y, color);
//                    This is a problem, needs optimization, O(N^2) is BAD
//                    jk its like .1% of processor time lol
                }
//                                    img.setRGB(x, y, mandelbrotData[x][y] > -1 ? Color.RED.getRGB() : Color.BLACK.getRGB());
//                                      //uncomment above line for box tracing    
            }
        }
        long stop = System.currentTimeMillis();
        System.out.println("Pixel coloring took " + (stop - start) + " ms");
    }

    public void setColorScheme(int colorScheme) {
        this.colorScheme = colorScheme;
    }

    public int getPixelShiftDistance() {
        return numShifts;
    }

    public void setPixelShiftDistance(int pixelShiftDistance) {
        this.numShifts = pixelShiftDistance;
    }

    void setInputSource(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

}
