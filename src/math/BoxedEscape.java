package math;

import architecture.Pool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import static math.MandelbrotCalculator.MAX_ITERATIONS;

/**
 *
 * @author David
 */
public class BoxedEscape extends Thread {

    private final int[][] buffer;
    private volatile NumberType[] xCoords;
    private volatile NumberType[] yCoords;
    private int val;
    public static final int NOT_CALCULATED_CONST = -1;
    private Pool<MRectangle> inPool;
    private Pool<MRectangle> outPool;

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
            while (!inPool.isEmpty()) {
                render(inPool.remove(0));
            }
        }
    }

    private int escape(NumberType x_curr, NumberType y_curr) {
        //Making new hashset is faster than an clearing existing hashset. Thanks, Thomas
        return x_curr.escape(x_curr, y_curr, new HashSet<>(), MAX_ITERATIONS);
    }

    public boolean testBox(MRectangle e) {
        //System.out.println(e.height);
        int yMax = e.y + e.height - 1;
        //System.out.println("ymax " +  yMax);
        int xMax = e.x + e.width - 1;
        //System.out.println(xMax + " " + yMax);
        if (buffer[e.x][e.y] == NOT_CALCULATED_CONST) {
            buffer[e.x][e.y] = escape(xCoords[e.x], yCoords[e.y]);
        }
        if (buffer[xMax][e.y] == NOT_CALCULATED_CONST) {
            buffer[xMax][e.y] = escape(xCoords[xMax], yCoords[e.y]);
        }
        if (buffer[e.x][yMax] == NOT_CALCULATED_CONST) {
            buffer[e.x][yMax] = escape(xCoords[e.x], yCoords[yMax]);
        }
        if (buffer[xMax][yMax] == NOT_CALCULATED_CONST) {
            buffer[xMax][yMax] = escape(xCoords[xMax], yCoords[yMax]);
        }
        val = buffer[e.x][e.y];

        //System.out.println(val);
        if (buffer[e.x][yMax] != val) {
            return false;
        }
        if (buffer[xMax][e.y] != val) {
            return false;
        }
        if (buffer[xMax][yMax] != val) {
            return false;
        }
        xLoop:
        for (int x = e.x + 1; x < xMax; x++) { //corners left off, should have been calculated earlier
            if (buffer[x][e.y] == NOT_CALCULATED_CONST) {
                buffer[x][e.y] = escape(xCoords[x], yCoords[e.y]);
            }
            if (buffer[x][e.y] != val) {
                return false;
            }
            if (buffer[x][yMax] == NOT_CALCULATED_CONST) {
                buffer[x][yMax] = escape(xCoords[x], yCoords[yMax]);
            }
            if (buffer[x][yMax] != val) {
                return false;
            }
        }
        yLoop:
        for (int y = e.y + 1; y < yMax; y++) {//corners left off, should have been calculated earlier
            if (buffer[e.x][y] == NOT_CALCULATED_CONST) {
                buffer[e.x][y] = escape(xCoords[e.x], yCoords[y]);
            }

            if (buffer[e.x][y] != val) {
                return false;
            }

            if (buffer[xMax][y] == NOT_CALCULATED_CONST) {
                buffer[xMax][y] = escape(xCoords[xMax], yCoords[y]);
            }
            if (buffer[xMax][y] != val) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the buffer array
     *
     * @param e
     */
    public void iteratePlain(MRectangle e) {
        for (int x = e.x; x < e.x + e.width; x++) {
            for (int y = e.y; y < e.y + e.height; y++) {
                if (buffer[x][y] == NOT_CALCULATED_CONST) {
                    buffer[x][y] = escape(xCoords[x], yCoords[y]);
                }
                //System.out.println(xCurr + " " + yCurr + "i");
                //System.out.println(buffer[x][y] + "\n");
                //System.out.println(escape_value);
            }
            //System.out.println("Line " + xCurr + " \\ " + data.length);
        }

    }

    private void render(MRectangle e) {
        boolean q = testBox(e);
        if (q) {
            for (int x = e.x; x < e.x + e.width; x++) {
                for (int y = e.y; y < e.y + e.height; y++) {
                    buffer[x][y] = val;
                }
            }
            e.setFilled(false);
            outPool.add(e);
            return;
        }
        //dVal is the discrepancy between the edges, we use it to detect structure
        int a, b, c, d;
        a = Math.max(buffer[e.x][e.y], buffer[e.x + e.width - 1][e.y]);
        b = Math.min(buffer[e.x][e.y], buffer[e.x + e.width - 1][e.y]);
        
        c = Math.max(buffer[e.x + e.width - 1][e.y + e.height - 1], buffer[e.x][e.y + e.height - 1]);
        d = Math.min(buffer[e.x + e.width - 1][e.y + e.height - 1], buffer[e.x][e.y + e.height - 1]);
        
        a = Math.max(a,c);
        b = Math.min(b,d);
 
        if (a - b > e.width * e.height * 10 || e.width < 5 || e.height < 5) {
            iteratePlain(e);
            e.setFilled(true);
            outPool.add(e);
        } else {
            int dx = e.width / 2; 
            int dy = e.height / 2;
            //System.out.println("splitting");
            inPool.add(new MRectangle(e.x, e.y, dx, dy));
            inPool.add(new MRectangle(e.x + dx , e.y, dx + e.width % 2, dy));
            inPool.add(new MRectangle(e.x, e.y + dy, dx, dy + e.height % 2));
            inPool.add(new MRectangle(e.x + dx, e.y + dy, dx + e.width %2 , dy + e.height % 2));
            //System.out.println(inPool.getValues());
        }

    }

    public void setxCoords(NumberType[] xCoords) {
        this.xCoords = xCoords;
    }

    public void setyCoords(NumberType[] yCoords) {
        this.yCoords = yCoords;
    }

    public void setInPool(Pool<MRectangle> inPool) {
        this.inPool = inPool;
    }

    public void setOutPool(Pool<MRectangle> outPool) {
        this.outPool = outPool;
    }

}
