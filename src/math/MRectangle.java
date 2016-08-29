/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import java.awt.Rectangle;

/**
 *
 * @author David
 */
public class MRectangle extends Rectangle{
    private final boolean filled;

    public MRectangle(boolean filled, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.filled = filled;
    }

    public boolean isFilled() {
        return filled;
    }
    
}
