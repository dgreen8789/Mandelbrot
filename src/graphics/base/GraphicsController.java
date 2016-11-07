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
        WINDOW_COLOR_UPDATE, REFRESH, SUPER_SAMPLE_TOGGLE, INCREASE_SUPER_SAMPLE,
        DECREASE_SUPER_SAMPLE, SHOW_BOXES, JULIA_KEY, TILT_FORWARD, TILT_BACKWARD,
        SAVE_TO_FILE, WINDOW_SOFT_ZOOM_IN_UPDATE, WINDOW_SOFT_ZOOM_OUT_UPDATE
    }
    public static final int ANYTHING = 7;
    public static int MAX_SHIFT_DISTANCE;
    public static final int MAX_SUPER_SAMPLE_FACTOR = 9;
    private static final double CONST_PAN_COEFF = 0;
    private static final double LINEAR_PAN_COEFF = 1;
    private static final double QUADRATIC_PAN_COEFF = 1;
    private static final double DELTA_THETA = Math.PI / 25;
    public static final double THETA_MAX = Math.PI / 2;
    public static final double THETA_MIN = 0;
    private static final int TARGET_SIZE = 20;

    public static final int THREAD_COUNT = 4;

    private final FractalRenderer[] calculators;
    private int currentRenderer;
    private final ColorScheme[] schemes;
    private int colorScheme;
    private final int width;
    private final int height;
    private static final boolean SAVE_IMAGES_TO_FILE = true;
    private static final String IMAGE_PATH = "C:\\Mandelbrot Image Logs";

    private final Point targetLocation;
    private boolean targetActive;
    private InputHandler inputHandler;
    private final ImageWriter writer;
    private double viewingAngle;

    public GraphicsController(int w, int h, Insets insets) {
        this.width = w - insets.right - insets.left;
        this.height = h - insets.top - insets.bottom;
        System.out.println(width + ", " + height);
        currentRenderer = 0;
        calculators = new FractalRenderer[2];
        calculators[0] = new MandelbrotRenderer(width, height, 0);
        calculators[0].resetData();
        DoubleNT c0 = new DoubleNT(500);
        DoubleNT c1 = new DoubleNT(500);
        calculators[1] = new JuliaRenderer(width, height, 0, c0, c1);
        schemes = ColorScheme.values();
        writer = SAVE_IMAGES_TO_FILE ? new ImageWriter(IMAGE_PATH, width, height) : null;
        MAX_SHIFT_DISTANCE = Math.min(w, h) - 1;
        targetLocation = new Point(width / 2, height / 2);
        targetActive = false;
        viewingAngle = THETA_MAX;

        //oldSS = -1;
    }

    private int determineShiftDistance(int consecutiveShifts) {
        return (int) Math.min(CONST_PAN_COEFF + consecutiveShifts * (LINEAR_PAN_COEFF
                + consecutiveShifts * QUADRATIC_PAN_COEFF), MAX_SHIFT_DISTANCE);
    }

    private int numShifts = 1;
    private GraphicsOperation lastCommand;

    void render(Graphics2D g, ArrayList<GraphicsOperation> input) {
        //System.out.println(input.contains(g));
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
            } else {
                input.remove(GraphicsOperation.JULIA_KEY);
                //lastCommand = null;
                targetActive = true;
            }
            lastCommand = GraphicsOperation.JULIA_KEY;
            //return;
        }
        if (input.contains(GraphicsOperation.TILT_BACKWARD)) {

            viewingAngle = Math.min(THETA_MAX, viewingAngle - DELTA_THETA);
            input.remove(GraphicsOperation.TILT_BACKWARD);
        }
        if (input.contains(GraphicsOperation.TILT_FORWARD)) {
            viewingAngle = Math.max(THETA_MIN, viewingAngle + DELTA_THETA);
            input.remove(GraphicsOperation.TILT_FORWARD);
        }
        int panDistance = determineShiftDistance(numShifts);
        if (!input.isEmpty()) {
            if (writer != null && input.contains(GraphicsOperation.SAVE_TO_FILE)) {
                writer.writeToFile(calculators[currentRenderer].getImage(), calculators[currentRenderer].getWindow());
            }
            if (input.contains(GraphicsOperation.WINDOW_HARD_ZOOM_IN_UPDATE)) {
                calculators[currentRenderer].getHistogram().reset();
                calculators[currentRenderer].zoom(true, inputHandler.getMousePoint(), MandelbrotRenderer.HARD_ZOOM_FACTOR);
                lastCommand = GraphicsOperation.WINDOW_HARD_ZOOM_IN_UPDATE;
            } else if (input.contains(GraphicsOperation.WINDOW_SOFT_ZOOM_IN_UPDATE)) {
                calculators[currentRenderer].getHistogram().reset();
                calculators[currentRenderer].zoom(true, inputHandler.getMousePoint(),
                        Math.pow(MandelbrotRenderer.SOFT_ZOOM_FACTOR, inputHandler.getScrollDistance()));
                lastCommand = GraphicsOperation.WINDOW_SOFT_ZOOM_IN_UPDATE;
            }
            if (input.contains(GraphicsOperation.WINDOW_HARD_ZOOM_OUT_UPDATE)) {
                calculators[currentRenderer].getHistogram().reset();

                calculators[currentRenderer].zoom(false, inputHandler.getMousePoint(), MandelbrotRenderer.HARD_ZOOM_FACTOR);
                lastCommand = GraphicsOperation.WINDOW_HARD_ZOOM_OUT_UPDATE;
            } else if (input.contains(GraphicsOperation.WINDOW_SOFT_ZOOM_OUT_UPDATE)) {
                calculators[currentRenderer].getHistogram().reset();
                calculators[currentRenderer].zoom(false, inputHandler.getMousePoint(),
                        Math.pow(MandelbrotRenderer.SOFT_ZOOM_FACTOR, inputHandler.getScrollDistance()));
                lastCommand = GraphicsOperation.WINDOW_SOFT_ZOOM_OUT_UPDATE;
            }
//            while (calculators[1].getCurrentSystem() != calculators[0].getCurrentSystem()) {
//                Class nextSystem = NUMBER_SYSTEMS[(calculators[1].getCurrentSystem() + 1) % NUMBER_SYSTEMS.length];
//                calculators[1].changeNumberSystem(nextSystem);
//            }

            if (input.contains(GraphicsOperation.WINDOW_PAN_RIGHT_UPDATE)) {
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
            if (input.contains(GraphicsOperation.WINDOW_PAN_LEFT_UPDATE)) {
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
            if (input.contains(GraphicsOperation.WINDOW_PAN_DOWN_UPDATE)) {
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
            if (input.contains(GraphicsOperation.WINDOW_PAN_UP_UPDATE)) {
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
        g.setColor(Color.red);
        String str = calculators[currentRenderer].getWindow().toPresentationString();
        g.drawImage(img, null, 0, 0);
        int textLength = Math.min(width, str.length() / 75 * width);
        g.setFont(GraphicsUtilities.fillRect(str, g, textLength, MAX_TEXT_HEIGHT));
        g.drawString(str, 0, height - MAX_TEXT_HEIGHT / 2);
//            if (superSample) {
//                str = superSampleFactor * superSampleFactor + "x";
//                g.setFont(GraphicsUtilities.fillRect(str, g, width / 10, MAX_TEXT_HEIGHT));
//                g.drawString(str, 50, 50);
//            }
        if (targetActive) {
            drawTarget(targetLocation.x, targetLocation.y, TARGET_SIZE, g);
        }
        if (drawBoxes && calculators[currentRenderer].hasBoxes()) {
            for (MRectangle r : calculators[currentRenderer].getBoxes()) {
                if (r == null) {
                } else //System.out.println(r);
                 if (r.isPixelCalculated()) {

                        g.setColor(Color.MAGENTA);
                        g.draw(r);
                    } else {
                        if (r.isIsHistorical()) {
                            g.setColor(Color.CYAN);
                        } else {
                            g.setColor(Color.BLUE);
                        }
                        g.draw(r);
                    }
            }
        }

    }
//private int oldSS;
    private boolean drawBoxes = false;
    //private boolean superSample = false;

    private static final int MAX_TEXT_HEIGHT = 40;

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

    void drawTarget(int x, int y, int size, Graphics2D g) {

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
