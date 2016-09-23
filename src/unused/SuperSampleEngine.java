/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unused;

import architecture.Pool;
import graphics.colors.Histogram;
import java.util.ArrayList;
import java.util.Arrays;
import math.BoxedEscape;
import math.MRectangle;
import math.numbertypes.NumberType;
import architecture.Window;

/**
 *
 * @author David
 */
public class SuperSampleEngine {

    private Histogram histogram;
    private final BoxedEscape threads[];

    public SuperSampleEngine(Histogram h, BoxedEscape[] threads) {
        this.histogram = h;
        this.threads = threads;
    }

    public Pool<MRectangle> computeSS(int factor, int[][] data, Window window, ArrayList<MRectangle> boxes) {
        System.out.println("called with " + boxes.size() + " boxes");
        Pool<MRectangle> outPool = new Pool<>();
        if (factor > 1) {
            NumberType[] xCoordsSS = new NumberType[data.length * factor];
            NumberType[] yCoordsSS = new NumberType[data[1].length * factor];
            NumberType xEpsilon = window.xRange.divide(data.length).mult2();
            NumberType yEpsilon = window.yRange.divide(data[0].length).mult2();
            xCoordsSS[0] = window.xCenter.subtract(window.xRange);
            for (int i = 1; i < xCoordsSS.length; i++) {
                xCoordsSS[i] = xCoordsSS[i - 1].add(xEpsilon);
            }
            yCoordsSS[0] = window.yCenter.subtract(window.yRange);
            for (int i = 1; i < yCoordsSS.length; i++) {
                yCoordsSS[i] = yCoordsSS[i - 1].add(yEpsilon);
            }
            final int[][] ssBuffer = new int[xCoordsSS.length][yCoordsSS.length];
            for (int[] is : ssBuffer) {
                Arrays.fill(is, BoxedEscape.NOT_CALCULATED_CONST);
            }
            System.out.println("Super sample buffer is " + ssBuffer.length + " x " + ssBuffer[0].length);
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    ssBuffer[factor * i][factor * j] = data[i][j];
                }
            }
            Pool<MRectangle> inPool = scale(boxes, factor, data.length / 2, data[0].length / 2);
            for (BoxedEscape x : threads) {
                x.setBuffer(ssBuffer);
                x.setInPool(inPool);
                x.setOutPool(outPool);
                x.setxCoords(xCoordsSS);
                x.setyCoords(yCoordsSS);
                x.interrupt();
            }
            while (calculating(threads) || !inPool.isEmpty()) {
                //System.out.println(inPool.getValues().size());
            }
            scaleDown(ssBuffer, data, factor);
            return scale(outPool.getValues(), 1 / factor, ssBuffer.length / 2, ssBuffer[0].length / 2);
        }
        outPool.addAll(boxes);
        return outPool;
    }

    private boolean calculating(BoxedEscape[] threads) {
        boolean done_calculating = true;
        for (BoxedEscape thread : threads) {
            done_calculating &= (thread.getState().equals(Thread.State.TIMED_WAITING));
        }
        return !done_calculating;
    }

    private Pool<MRectangle> scale(ArrayList<MRectangle> boxes, double factor, int xCenter, int yCenter) {
        final Pool<MRectangle> pool = new Pool<>();
        final int newXCenter = (int) (xCenter * factor);
        final int newYCenter = (int) (yCenter * factor);
        boxes.stream().map(r -> new MRectangle(
                (int) ((r.x - xCenter) * factor + newXCenter),
                (int) ((r.y - yCenter) * factor + newYCenter),
                (int) (r.width * factor),
                (int) (r.height * factor)
        )).forEach(r -> pool.add((MRectangle) r));
        return pool;
    }

    private void scaleDown(int[][] ssBuffer, int[][] data, int factor) {
        int k = factor / 2;
        for (int i = 0; i < data.length; i++) {
            int xC = i * factor + factor / 2;
            for (int j = 0; j < data[0].length; j++) {
                histogram.decrement(data[i][j]);
                int yC = j * factor + factor / 2;
                for (int l = xC - k; l < xC + k; l++) {
                    for (int m = yC - k; m < yC + k; m++) {
                        data[i][j] += ssBuffer[l][m];
                    }
                }
                data[i][j] /= factor * factor;
                histogram.increment(data[i][j]);
            }
        }
//        }
    }

}
