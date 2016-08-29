package math;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import static math.MandelbrotCalculator.MAX_ITERATIONS;

/**
 *
 * @author David
 */
public class BoxedEscape extends Thread {

    private final int[][] buffer;
    private volatile  NumberType[] xCoords;
    private volatile  NumberType[] yCoords;
    private int miniWindow[];
    private int val;
    private static final int NOT_CALCULATED_CONST = -1;
    private static ArrayList<MRectangle> boxes;
    public BoxedEscape(NumberType[] xCoords, NumberType[] yCoords, int[][] buffer) {
        this.xCoords = xCoords;
        this.yCoords = yCoords;
        this.buffer = buffer;
    }

    private static final long sleepTime = Long.MAX_VALUE;

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
            }
            for (int i = miniWindow[0]; i < miniWindow[1]; i++) {
                Arrays.fill(buffer[i], miniWindow[2], miniWindow[3], NOT_CALCULATED_CONST);
            }
            boxes = new ArrayList<>();
            render(miniWindow[0], miniWindow[1], miniWindow[2], miniWindow[3]);
        }
    }

    //hashlist for the escape algorithm
    private HashSet<Integer> hashes;

    private int escape(NumberType x_curr, NumberType y_curr) {
        //Faster than clearing existing hashset. Thanks, Thomas
        hashes = new HashSet<>(); 
        // System.out.println(x_curr);
        return x_curr.escape(x_curr, y_curr, hashes, MAX_ITERATIONS);
    }

    public void setMiniWindow(int[] minWindow) {
        this.miniWindow = minWindow;
    }

    public int testBox(int xMin, int xMax, int yMin, int yMax) {
        int dVal;
        if (buffer[xMin][yMin] == NOT_CALCULATED_CONST) {
            buffer[xMin][yMin] = escape(xCoords[xMin], yCoords[yMin]);
        }
        if (buffer[xMax][yMin] == NOT_CALCULATED_CONST) {
            buffer[xMax][yMin] = escape(xCoords[xMax], yCoords[yMin]);
        }
        if (buffer[xMin][yMax] == NOT_CALCULATED_CONST) {
            buffer[xMin][yMax] = escape(xCoords[xMin], yCoords[yMax]);
        }
        if (buffer[xMax][yMax] == NOT_CALCULATED_CONST) {
            buffer[xMax][yMax] = escape(xCoords[xMax], yCoords[yMax]);
        }
        val = buffer[xMin][yMin];
        dVal = Math.max(Math.max(buffer[xMax][yMax], buffer[xMax][yMin]), Math.max(buffer[xMin][yMin], buffer[xMin][yMax]))
                - Math.min(Math.min(buffer[xMax][yMax], buffer[xMax][yMin]), Math.min(buffer[xMin][yMin], buffer[xMin][yMax]));
        //System.out.println(val);
        if (buffer[xMin][yMax] != val) {
            return dVal;
        }
        if (buffer[xMax][yMin] != val) {
            return dVal;
        }
        if (buffer[xMax][yMax] != val) {
            return dVal;
        }
        xLoop:
        for (int x = xMin + 1; x < xMax; x++) {
            if (buffer[x][yMin] == NOT_CALCULATED_CONST) {
                buffer[x][yMin] = escape(xCoords[x], yCoords[yMin]);
            }
            if (buffer[x][yMin] != val) {
                return dVal;
            }
            if (buffer[x][yMax] == NOT_CALCULATED_CONST) {
                buffer[x][yMax] = escape(xCoords[x], yCoords[yMax]);
            }
            if (buffer[x][yMax] != val) {
                return dVal;
            }
        }
        yLoop:
        for (int y = yMin + 1; y < yMax; y++) {
            if (buffer[xMin][y] == NOT_CALCULATED_CONST) {
                buffer[xMin][y] = escape(xCoords[xMin], yCoords[y]);
            }

            if (buffer[xMin][y] != val) {
                return dVal;
            }

            if (buffer[xMax][y] == NOT_CALCULATED_CONST) {
                buffer[xMax][y] = escape(xCoords[xMax], yCoords[y]);
            }
            if (buffer[xMax][y] != val) {
                return dVal;
            }
        }
        boxes.add(new MRectangle(false, xMin, yMin, xMax - xMin, yMax - yMin));
        return NOT_CALCULATED_CONST; //0
    }

    /**
     * Updates the buffer array and the histogram;
     *
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     */
    public void iteratePlain(int xMin, int xMax, int yMin, int yMax) {
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                if (buffer[x][y] == NOT_CALCULATED_CONST) {
                    buffer[x][y] = escape(xCoords[x], yCoords[y]);
                }
                //System.out.println(xCurr + " " + yCurr + "i");
                //System.out.println(buffer[x][y] + "\n");
                //System.out.println(escape_value);
            }
            //System.out.println("Line " + xCurr + " \\ " + data.length);
        }
        boxes.add(new MRectangle(true, xMin, yMin, xMax - xMin, yMax - yMin));
    }

    private void render(int xMin, int xMax, int yMin, int yMax) {
        int dx = xMax - xMin;
        int dy = yMax - yMin;
        boolean a = dx < 5;
        boolean b = dy < 5;
        int dVal = testBox(xMin, xMax - 1, yMin, yMax - 1); //dVal is the discrepancy between the edges, we use it to detect structure
        if (dVal == NOT_CALCULATED_CONST) { //we test if NOT_CALCULATED_CONST because dVal can be zero but the rectangle is not valid
            //System.out.println("called");
            for (int x = xMin; x < xMax; x++) {
                for (int y = yMin; y < yMax; y++) {
                   buffer[x][y] = val; //comment this for box tracing
                }
            }

        } else //System.out.println("not called");
        {
            if (dVal > dx * dy * 10) {
                iteratePlain(xMin, xMax, yMin, yMax);
            } else if (a || b) {
                if (a && b) {
                    iteratePlain(xMin, xMax, yMin, yMax);
                } else if (b) {
                    render(xMin, xMin + dx / 4, yMin, yMax);
                    render(xMin + dx / 4, xMin + dx / 2, yMin, yMax);
                    render(xMin + dx / 2, xMin + 3 * dx / 4, yMin, yMax);
                    render(xMin + 3 * dx / 4, xMax, yMin, yMax);
                } else {
                    render(xMin, xMax, yMin, yMin + dy / 4);
                    render(xMin, xMax, yMin + dy / 4, yMin + dy / 2);
                    render(xMin, xMax, yMin + dy / 2, yMin + 3 * dy / 4);
                    render(xMin, xMax, yMin + 3 * dy / 4, yMax);
                }
            } else {
                //System.out.println("splitting");
                dx = xMin + dx / 2;
                dy = yMin + dy / 2;
                render(xMin, dx, yMin, dy);
                render(xMin, dx, dy, yMax);
                render(dx, xMax, yMin, dy);
                render(dx, xMax, dy, yMax);
            }
        }
    }

    public void setxCoords(NumberType[] xCoords) {
        this.xCoords = xCoords;
    }

    public void setyCoords(NumberType[] yCoords) {
        this.yCoords = yCoords;
    }

    public ArrayList<MRectangle> getBoxes() {
        return boxes;
    }
    
}
