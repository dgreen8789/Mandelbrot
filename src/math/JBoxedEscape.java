/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import graphics.colors.Histogram;
import java.util.HashSet;
import math.numbertypes.NumberType;

/**
 *
 * @author David
 */
public class JBoxedEscape extends BoxedEscape{
    private NumberType x0;
    private NumberType y0;
    public JBoxedEscape(NumberType[] xCoords, NumberType[] yCoords, int[][] data, boolean[][] valid, Histogram histogram) {
        super(xCoords, yCoords, data, valid, histogram);
    }

    @Override
    protected int escape(NumberType x_curr, NumberType y_curr) {
        int z = x_curr.JEscape(x_curr, y_curr, x0, y0, new HashSet<>(), MandelbrotRenderer.MAX_ITERATIONS); 
        return z;
    }

    public void setX0(NumberType x0) {
        this.x0 = x0;
    }

    public void setY0(NumberType y0) {
        this.y0 = y0;
    }

  
//
//    @Override
//    public boolean testBox(MRectangle e) {
//        return connected && super.testBox(e); //To change body of generated methods, choose Tools | Templates.
//    }
    
}
