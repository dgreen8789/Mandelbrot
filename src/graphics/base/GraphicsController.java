package graphics.base;

import math.FractalRenderer;
import graphics.colors.ColorScheme;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.JFrame;
import math.JuliaRenderer;
import math.MRectangle;
import math.MandelbrotRenderer;
import math.numbertypes.DoubleNT;
import math.numbertypes.NumberType;

/**
 *
 * @author David
 */
public class GraphicsController {

    public enum GraphicsOperation {
        WINDOW_HARD_ZOOM_IN_UPDATE, WINDOW_HARD_ZOOM_OUT_UPDATE, WINDOW_PAN_UP_UPDATE,
        WINDOW_PAN_DOWN_UPDATE, WINDOW_PAN_LEFT_UPDATE, WINDOW_PAN_RIGHT_UPDATE,
        WINDOW_COLOR_UPDATE, REFRESH, RESIZE, SUPER_SAMPLE_TOGGLE, INCREASE_SUPER_SAMPLE,
        DECREASE_SUPER_SAMPLE, SHOW_BOXES, JULIA_KEY, TILT_FORWARD, TILT_BACKWARD,
        SAVE_TO_FILE, INCREASE_PRECISION, DECREASE_PRECISION, WINDOW_MOUSE_ZOOM_UPDATE
    }
    public static int MAX_SHIFT_DISTANCE;
    public static int MAX_SMOOTH_SHIFT_DISTANCE;
    public static final int MAX_SUPER_SAMPLE_FACTOR = 9;
    private static final double CONST_PAN_COEFF = 0;
    private static final double LINEAR_PAN_COEFF = 1;
    private static final double QUADRATIC_PAN_COEFF = 1;
    private static final double DELTA_THETA = Math.PI / 25;
    public static final double THETA_MAX = Math.PI / 2;
    public static final double THETA_MIN = 0;
    public static final int MAX_SMOOTH_DISTANCE = 10;
    public static final int TARGET_FPS = 30;
    private static final int TARGET_SIZE = 20;

    public static final int THREAD_COUNT = 4;

    private final FractalRenderer[] calculators;
    private int currentRenderer;
    private final ColorScheme[] schemes;
    private int colorScheme;
    private int width;
    private int height;
    private static final boolean SAVE_IMAGES_TO_FILE = true;
    private static final String IMAGE_PATH = "C:\\Mandelbrot Image Logs";

    private final Point targetLocation;
    private boolean targetActive;
    private InputHandler inputHandler;
    private final ImageWriter writer;
    private double viewingAngle;
    private final JFrame frame;
    private long renderStartTime;
    HashSet<GraphicsOperation> renderOperations;

    public GraphicsController(JFrame frame) {
        this.frame = frame;
        Insets insets = frame.getInsets();
        this.width = frame.getWidth() - insets.right - insets.left;
        this.height = frame.getHeight() - insets.top - insets.bottom;
        System.out.println(width + ", " + height);
        currentRenderer = 0;
        calculators = new FractalRenderer[2];
        calculators[0] = new MandelbrotRenderer(width, height);
        calculators[0].resetData();
        DoubleNT c0 = new DoubleNT(500);
        DoubleNT c1 = new DoubleNT(500);
        calculators[1] = new JuliaRenderer(width, height, c0, c1);
        schemes = ColorScheme.values();
        writer = SAVE_IMAGES_TO_FILE ? new ImageWriter(IMAGE_PATH, width, height) : null;
        MAX_SHIFT_DISTANCE = Math.min(width, height) - 1;
        MAX_SMOOTH_SHIFT_DISTANCE = 10;
        targetLocation = new Point(width / 2, height / 2);
        targetActive = false;
        viewingAngle = THETA_MAX;

        //oldSS = -1;
        renderOperations = new HashSet<>();
        renderOperations.addAll(Arrays.asList(new GraphicsOperation[]{
            GraphicsOperation.WINDOW_HARD_ZOOM_IN_UPDATE,
            GraphicsOperation.WINDOW_HARD_ZOOM_OUT_UPDATE,
            GraphicsOperation.WINDOW_PAN_UP_UPDATE,
            GraphicsOperation.WINDOW_PAN_DOWN_UPDATE,
            GraphicsOperation.WINDOW_PAN_LEFT_UPDATE,
            GraphicsOperation.WINDOW_PAN_RIGHT_UPDATE,
            GraphicsOperation.WINDOW_MOUSE_ZOOM_UPDATE,
            GraphicsOperation.REFRESH}));
    }

    private int determineShiftDistance(int consecutiveShifts) {
        return (int) Math.min(CONST_PAN_COEFF + consecutiveShifts * (LINEAR_PAN_COEFF
                + consecutiveShifts * QUADRATIC_PAN_COEFF), MAX_SHIFT_DISTANCE);

    }

    private int numShifts = 1;
    private GraphicsOperation lastCommand;
    private double renderTime;
    private int panDistance;

    void render(Graphics2D g, ArrayList<GraphicsOperation> input) {
        //System.out.println(input.contains(g));
        if (input.contains(GraphicsOperation.RESIZE)) {
            Insets insets = frame.getInsets();
            this.width = frame.getWidth() - insets.right - insets.left;
            this.height = frame.getHeight() - insets.top - insets.bottom;
            for (FractalRenderer fr : calculators) {
                fr.resize(width, height);
            }
            input.remove(GraphicsOperation.RESIZE);
            return;
        }
        double prop = calculators[currentRenderer].propPixelsRendered();
        if (prop < 1) {
            renderTime = (System.currentTimeMillis() - renderStartTime) / 1000.0;
        }
        boolean smoothPan = inputHandler.isScrollLockOn();
        //System.out.println(smoothPan);
        boolean canPan = prop >= 1 || !smoothPan;
        panDistance = canPan && isPan(lastCommand) ? dynamicPanDistance(renderTime, panDistance, TARGET_FPS)
                : determineShiftDistance(numShifts);
        if ((input.contains(GraphicsOperation.JULIA_KEY))) {
            //System.out.println("PRESSED");
            if (currentRenderer == 1) {
                targetActive = false;
                currentRenderer = 0;
            } else if (targetActive) {
                NumberType[] c = calculators[currentRenderer].coordinateByPoint(targetLocation);
                ((JuliaRenderer) calculators[1]).setConstant(c[0], c[1]);
                calculators[1].resetWindow();
                calculators[1].resetData();
                calculators[1].draw();
                currentRenderer = ++currentRenderer % calculators.length;
                targetActive = false;
                input.clear();
                renderStartTime = System.currentTimeMillis();
            } else {
                input.remove(GraphicsOperation.JULIA_KEY);
                //lastCommand = null;
                targetActive = true;
            }
            lastCommand = GraphicsOperation.JULIA_KEY;
            //return;
        }
        HashSet<GraphicsOperation> clone = (HashSet<GraphicsOperation>) renderOperations.clone();
        clone.addAll(input);
        if (clone.size() == renderOperations.size() && !input.isEmpty()) {
            renderStartTime = System.currentTimeMillis();
        }
        if (input.contains(GraphicsOperation.INCREASE_PRECISION)) {
            calculators[currentRenderer].changeNumberSystem(true);
            input.remove(GraphicsOperation.INCREASE_PRECISION);
        }
        if (input.contains(GraphicsOperation.DECREASE_PRECISION)) {
            calculators[currentRenderer].changeNumberSystem(false);
            input.remove(GraphicsOperation.DECREASE_PRECISION);
        }
        if (input.contains(GraphicsOperation.TILT_BACKWARD)) {

            viewingAngle = Math.min(THETA_MAX, viewingAngle - DELTA_THETA);
            input.remove(GraphicsOperation.TILT_BACKWARD);
        }
        if (input.contains(GraphicsOperation.TILT_FORWARD)) {
            viewingAngle = Math.max(THETA_MIN, viewingAngle + DELTA_THETA);
            input.remove(GraphicsOperation.TILT_FORWARD);
        }

        if (!input.isEmpty()) {
            if (writer != null && input.contains(GraphicsOperation.SAVE_TO_FILE)) {
                writer.writeToFile(
                        calculators[currentRenderer].getImage(),
                        calculators[currentRenderer].getWindow(),
                        false);
            }
            if (input.contains(GraphicsOperation.WINDOW_HARD_ZOOM_IN_UPDATE)) {
                calculators[currentRenderer].getHistogram().reset();
                calculators[currentRenderer].zoom(true, inputHandler.getMousePoint(), MandelbrotRenderer.HARD_ZOOM_FACTOR);
                lastCommand = GraphicsOperation.WINDOW_HARD_ZOOM_IN_UPDATE;
            } else if (input.contains(GraphicsOperation.WINDOW_MOUSE_ZOOM_UPDATE)) {
                calculators[currentRenderer].getHistogram().reset();
                calculators[currentRenderer].zoom(false, inputHandler.getMousePoint(),
                        Math.pow(MandelbrotRenderer.SOFT_ZOOM_FACTOR, inputHandler.getMouseWheelScrollDistance()));
                lastCommand = GraphicsOperation.WINDOW_MOUSE_ZOOM_UPDATE;
            }
            if (input.contains(GraphicsOperation.WINDOW_HARD_ZOOM_OUT_UPDATE)) {
                calculators[currentRenderer].getHistogram().reset();
                calculators[currentRenderer].zoom(false, inputHandler.getMousePoint(), MandelbrotRenderer.HARD_ZOOM_FACTOR);
                lastCommand = GraphicsOperation.WINDOW_HARD_ZOOM_OUT_UPDATE;
            }
//            while (calculators[1].getCurrentSystem() != calculators[0].getCurrentSystem()) {
//                Class nextSystem = NUMBER_SYSTEMS[(calculators[1].getCurrentSystem() + 1) % NUMBER_SYSTEMS.length];
//                calculators[1].changeNumberSystem(nextSystem);
//            }

            if (input.contains(GraphicsOperation.WINDOW_PAN_RIGHT_UPDATE) && canPan) {
                if (lastCommand == GraphicsOperation.WINDOW_PAN_RIGHT_UPDATE) {
                    numShifts++;
                } else {
                    numShifts = 1;
                }
                lastCommand = GraphicsOperation.WINDOW_PAN_RIGHT_UPDATE;
                if (targetActive) {
                    targetLocation.translate(Math.min(width - targetLocation.x, panDistance), 0);
                } else {
                    calculators[currentRenderer].panRight(panDistance);
                }
            }
            if (input.contains(GraphicsOperation.WINDOW_PAN_LEFT_UPDATE) && canPan) {
                if (lastCommand == GraphicsOperation.WINDOW_PAN_LEFT_UPDATE) {
                    numShifts++;
                } else {
                    numShifts = 1;
                }
                lastCommand = GraphicsOperation.WINDOW_PAN_LEFT_UPDATE;
                if (targetActive) {
                    targetLocation.translate(-Math.min(targetLocation.x, panDistance), 0);
                } else {
                    calculators[currentRenderer].panLeft(panDistance);
                }
            }
            if (input.contains(GraphicsOperation.WINDOW_PAN_DOWN_UPDATE) && canPan) {
                if (lastCommand == GraphicsOperation.WINDOW_PAN_DOWN_UPDATE) {
                    numShifts++;
                } else {
                    numShifts = 1;
                }
                lastCommand = GraphicsOperation.WINDOW_PAN_DOWN_UPDATE;
                if (targetActive) {
                    targetLocation.translate(0, Math.min(height - targetLocation.y, panDistance));
                } else {
                    calculators[currentRenderer].panDown(panDistance);
                }
            }
            if (input.contains(GraphicsOperation.WINDOW_PAN_UP_UPDATE) && canPan) {
                if (lastCommand == GraphicsOperation.WINDOW_PAN_UP_UPDATE) {
                    numShifts++;
                } else {
                    numShifts = 1;
                }
                lastCommand = GraphicsOperation.WINDOW_PAN_UP_UPDATE;
                if (targetActive) {
                    targetLocation.translate(0, -Math.min(targetLocation.y, panDistance));
                } else {
                    calculators[currentRenderer].panUp(panDistance);
                }
            }

            if ((input.contains(GraphicsOperation.REFRESH))) {
                System.out.println("called");
                System.out.println(calculators[currentRenderer].getWindow());
                calculators[currentRenderer].draw();
                lastCommand = GraphicsOperation.REFRESH;
            }
            //System.out.println(Arrays.deepToString(calculators[currentRenderer].getDataArray()));

//                if ((input.contains(GraphicsOperation.SUPER_SAMPLE_TOGGLE))) {
//                    superSample = !superSample;
//                }
//            if (superSample) {
//                if (oldSS != superSampleFactor) {
//                    calculator.superSample(oldSS, superSampleFactor);
//                }
//                oldSS = superSampleFactor;
//
//            }
        }
        // } else {

        //}
        if (input.contains(GraphicsOperation.WINDOW_COLOR_UPDATE)) {
            lastCommand = GraphicsOperation.WINDOW_COLOR_UPDATE;
            colorScheme = ++colorScheme % schemes.length;
            input.remove(GraphicsOperation.WINDOW_COLOR_UPDATE);
        }

        if ((input.contains(GraphicsOperation.SHOW_BOXES))) {
            drawBoxes = !drawBoxes;
            input.remove(GraphicsOperation.SHOW_BOXES);
        }

        input.clear();
        BufferedImage img = calculators[currentRenderer].createImage(true, schemes[colorScheme], viewingAngle);

        g.drawImage(img,
                null, 0, 0);
//            if (superSample) {
//                str = superSampleFactor * superSampleFactor + "x";
//                g.setFont(GraphicsUtilities.fillRect(str, g, width / 10, MAX_TEXT_HEIGHT));
//                g.drawString(str, 50, 50);
//            }
        if (targetActive) {
            g.setColor(Color.RED);
            drawTarget(targetLocation.x, targetLocation.y, TARGET_SIZE, g);
        }

        if (drawBoxes && calculators[currentRenderer].hasBoxes()) {
            for (MRectangle r : calculators[currentRenderer].getBoxes()) {
                if (r == null) {
                } else //System.out.println(r);
                {
                    if (r.isPixelCalculated()) {

                        g.setColor(Color.MAGENTA);
                        g.draw(r);
                    } else {
                        if (r.isHistorical()) {
                            g.setColor(Color.CYAN);
                        } else {
                            g.setColor(Color.BLUE);
                        }
                        g.draw(r);
                    }
                }
            }
        }

        StringBuilder title = new StringBuilder("Mandelbrot Grapher by David Green - ");
        title.append(String.format("Render %.2f%% complete in %.3f seconds. ", 100 * prop, renderTime));
        title.append(String.format("Current Zoom level is 10^%.4f. ", calculators[currentRenderer].getWindow().getZoomLevel()));
        title.append(String.format("Render speed - %.3f pixels per second. ", calculators[currentRenderer].numPixelsRendered() / renderTime));
        title.append("Press F1 for help.");
        frame.setTitle(title.toString());

    }
    //private int oldSS;
    private boolean drawBoxes = false;
    //private boolean superSample = false;

    public void setColorScheme(int colorScheme) {
        this.colorScheme = colorScheme;
    }

    boolean isPan(GraphicsOperation op) {
        return op == GraphicsOperation.WINDOW_PAN_DOWN_UPDATE
                || op == GraphicsOperation.WINDOW_PAN_UP_UPDATE
                || op == GraphicsOperation.WINDOW_PAN_LEFT_UPDATE
                || op == GraphicsOperation.WINDOW_PAN_RIGHT_UPDATE;

    }

    boolean isZoom(GraphicsOperation op) {
        return op == GraphicsOperation.WINDOW_HARD_ZOOM_IN_UPDATE
                || op == GraphicsOperation.WINDOW_HARD_ZOOM_OUT_UPDATE
                || op == GraphicsOperation.WINDOW_MOUSE_ZOOM_UPDATE;
    }

    public int getPixelShiftDistance() {
        return numShifts;
    }

    public void setPixelShiftDistance(int pixelShiftDistance) {
        this.numShifts = pixelShiftDistance;
    }

    public int dynamicPanDistance(double lastRenderTime, int lastPixels, int FPSTarget) {
        // return MAX_SMOOTH_DISTANCE;
//        System.out.println("VARS:");
//        System.out.println(lastRenderTime);
//        System.out.println(lastPixels);
//        System.out.println(FPSTarget);
//        System.out.println("");
//        int val = (int) (lastRenderTime * FPSTarget * lastPixels);
//        System.out.println("OUTPUT:");
//        System.out.println(val);
//        System.out.println("");
        return MAX_SHIFT_DISTANCE / 10;//Math.min(Math.max(1, val), MAX_SHIFT_DISTANCE / 10);
    }

    void setInputSource(InputHandler inputHandler
    ) {
        this.inputHandler = inputHandler;
    }

    void drawTarget(int x, int y, int size, Graphics2D g
    ) {

        size = size / 2;
        int xLeft = Math.min(size, x);
        int xRight = Math.min(size, width - x);
        int yTop = Math.min(size, y);
        int yBot = Math.min(size, height - y);
        g.fillRect(x, y, 1, 1);

        g.drawLine(x, y - 1, x, y - yTop);
        g.drawLine(x, y + 1, x, y + yBot);
        g.drawLine(x - 1, y, x - xLeft, y);
        g.drawLine(x + 1, y, x + xRight, y);

    }

}
