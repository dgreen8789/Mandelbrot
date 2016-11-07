/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import architecture.Pool;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 *
 * @author David
 */
public class MRectangle extends Rectangle implements Comparable {

    private boolean pixelCalculated;
    private boolean isHistorical;

    public MRectangle(boolean filled, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.pixelCalculated = filled;
    }

    public MRectangle(int x, int y, int width, int height) {
        super(x, y, width, height);
        pixelCalculated = false;
    }

    public boolean isPixelCalculated() {
        return pixelCalculated;
    }

    public void setPixelCalculated(boolean pixelCalculated) {
        this.pixelCalculated = pixelCalculated;
    }

    public int compareTo(Object o) {
        MRectangle x;
        if (o instanceof MRectangle) {
            x = (MRectangle) o;
            if (pixelCalculated ^ x.pixelCalculated) {
                return pixelCalculated ? -1 : 1;
            }
            return this.x == x.x ? this.y - x.y : this.x - x.x;
        } else {
            throw new ClassCastException(o + "is not an instance of MRectangle");
        }
    }

    //lifted straight from java source to avoid extra instantiation;
    //changed some longs to ints because extra size was unnessacary
    //removed safety checks for underflow
    @Override
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

    @Override
    public String toString() {
        return super.toString() + "\tfilled=" + pixelCalculated;
    }

    public boolean isIsHistorical() {
        return isHistorical;
    }

    public void setIsHistorical(boolean isHistorical) {
        this.isHistorical = isHistorical;
    }

    public static void split(MRectangle e, Pool<MRectangle> o) {
        //System.out.print("Before: " + o.size());
        int dx = e.width / 2;
        int dy = e.height / 2;
        //System.out.println("splitting");
        o.add(new MRectangle(e.x, e.y, dx, dy));
        o.add(new MRectangle(e.x + dx, e.y, dx + e.width % 2, dy));
        o.add(new MRectangle(e.x, e.y + dy, dx, dy + e.height % 2));
        o.add(new MRectangle(e.x + dx, e.y + dy, dx + e.width % 2, dy + e.height % 2));
        // System.out.println("\tAfter: " + o.size());
    }

    public static void split(MRectangle e, ArrayList<MRectangle> o, int pos) {
        //System.out.print("Before: " + o.size());
        int dx = e.width / 2;
        int dy = e.height / 2;
        //System.out.println("splitting");
        o.add(pos, new MRectangle(e.x, e.y, dx, dy));
        o.add(pos, new MRectangle(e.x + dx, e.y, dx + e.width % 2, dy));
        o.add(pos, new MRectangle(e.x, e.y + dy, dx, dy + e.height % 2));
        o.add(pos, new MRectangle(e.x + dx, e.y + dy, dx + e.width % 2, dy + e.height % 2));
        // System.out.println("\tAfter: " + o.size());
    }

}
