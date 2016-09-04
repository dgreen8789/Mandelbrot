/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author David
 */
class BoxTracer extends Thread {

    static void blowup(int factor, int[][] data) {
    }

    private int factor;
    private int[][] data;
    private NumberType xEpsilon;
    private NumberType yEpsilon;
    private List<MRectangle> rectangles;

    public void setFactor(int factor) {
        this.factor = factor;
    }



    public void setxEpsilon(NumberType xEpsilon) {
        this.xEpsilon = xEpsilon;
    }

    public void setyEpsilon(NumberType yEpsilon) {
        this.yEpsilon = yEpsilon;
    }

    void assign(List<MRectangle> subList) {
        rectangles = subList;
    }
    private static final long sleepTime = Long.MAX_VALUE;

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
            }
            computeSuperSample();
        }
    }

    private void computeSuperSample() {
        int[] temp = new int[factor * factor];
        for (MRectangle r : rectangles) {
            
            for (int dx = -factor / 2; dx <= factor / 2; dx++) {
                for (int dy = -factor / 2; dy <= factor / 2; dy++) {

                }
            }
        }
    }

}
