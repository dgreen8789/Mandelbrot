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

    private boolean filled;

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

    //lifted straight from java source to avoid extra instantiation;
    //changed some longs to ints because extra size was unnessacary
    //removed safety checks for underflow
    public MRectangle intersection(Rectangle r) {
        int tx1 = this.x;
        int ty1 = this.y;
        int rx1 = r.x;
        int ry1 = r.y;
        int tx2 = tx1;
        tx2 += this.width;
        int ty2 = ty1;
        ty2 += this.height;
        int rx2 = rx1;
        rx2 += r.width;
        int ry2 = ry1;
        ry2 += r.height;
        if (tx1 < rx1) {
            tx1 = rx1;
        }
        if (ty1 < ry1) {
            ty1 = ry1;
        }
        if (tx2 > rx2) {
            tx2 = rx2;
        }
        if (ty2 > ry2) {
            ty2 = ry2;
        }
        tx2 -= tx1;
        ty2 -= ty1;
        return new MRectangle(tx1, ty1, (int) tx2, (int) ty2);
    }
}
