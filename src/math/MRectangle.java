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
public class MRectangle extends Rectangle implements Comparable {

    private  boolean filled;

    public MRectangle(boolean filled, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.filled = filled;
    }

    public MRectangle(int x, int y, int width, int height) {
        super(x, y, width, height);
        filled = false;
    }

    public boolean isFilled() {
        return filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }
    
    public int compareTo(Object o) {
        MRectangle x;
        if (o instanceof MRectangle) {
            x = (MRectangle) o;
            if (filled ^ x.filled) {
                return filled ? -1 : 1;
            }
            return this.x == x.x ? this.y - x.y : this.x - x.x;
        } else {
            throw new ClassCastException(o + "is not an instance of MRectangle");
        }
    }

}
