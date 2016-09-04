/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import graphics.base.GraphicsController;
import graphics.colors.Histogram;
import java.util.ArrayList;

/**
 *
 * @author David
 */
public class SuperSampleEngine {
    private BoxTracer[] tracers;
    private Histogram histogram;
    public SuperSampleEngine(Histogram h){
        this.histogram = h;
        tracers = new BoxTracer[GraphicsController.THREAD_COUNT];
    }
    public void computeSS(int factor, int[][] data, NumberType xEpsilon, NumberType yEpsilon, ArrayList<MRectangle> boxes){
        int s = boxes.size();
        BoxTracer.blowup(factor, data);
        for (int i = 0; i < tracers.length; i++) {
            tracers[i].setxEpsilon(xEpsilon);
            tracers[i].setyEpsilon(yEpsilon);
            tracers[i].setFactor(factor);
            tracers[i].assign(boxes.subList(i * s, (i + 1) * s));
            tracers[i].interrupt();
            
        }
        boolean b = false;
        while(!b){
            b = true;
            for (int i = 0; i < tracers.length; i++) {
              b = b && tracers[i].getState() == Thread.State.TIMED_WAITING;
            }
        }
        
       
        
    }
}
