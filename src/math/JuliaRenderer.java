/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import architecture.Pool;
import architecture.Window;
import graphics.base.GraphicsController;
import java.util.HashSet;
import math.numbertypes.DoubleNT;
import math.numbertypes.NumberType;

/**
 *
 * @author David
 */
public class JuliaRenderer extends MandelbrotRenderer {

    //Together these form C
    protected NumberType cX;
    protected NumberType cY;
    private final boolean connected;

    public JuliaRenderer(int width, int height, NumberType cX, NumberType cY) {
        super(width, height, cX.getClass());
        threads = new JBoxedEscape[GraphicsController.THREAD_COUNT];
        this.cX = cX;
        this.cY = cY;
        for (BoxedEscape thread : threads) {
            if (thread != null) {
                thread.setxCoords(xCoords);
                thread.setyCoords(yCoords);
            }
        }
        while (this.window.xCenter.getClass() != cX.getClass()) {
            System.out.println("call");
            this.changeWindow(true, window);
        }
        connected = (this.cX.mEscape(this.cX, this.cY, new HashSet<>(), MAX_ITERATIONS) == MAX_ITERATIONS);
        System.out.println("The julia set for " + cX + " + " + cY + "i is " + (connected ? "" : " not ") + "connected");

    }

//    @Override
//    public boolean hasBoxes() {
//        return connected;
//    }
    @Override
    protected void beginRender(boolean killThreads, int x0, int y0, int x1, int y1) {
        //System.out.println("started render");
        inPool.clear();
        MRectangle.split(new MRectangle(x0, y0, x1, y1), inPool);
        outPool = new Pool<>();
        for (int i = 0; i < threads.length; i++) {
            if (killThreads) {
                if (threads[i] != null) {
                    threads[i].kill();
                }
                threads[i] = new JBoxedEscape(xCoords, yCoords, data, valid, histogram);
                threads[i].setInPool(inPool);
                ((JBoxedEscape) threads[i]).setX0(cX);
                ((JBoxedEscape) threads[i]).setY0(cY);
                threads[i].setOutPool(outPool);
                threads[i].setName("Drawer thread " + i);
                threads[i].start();
            } else if (threads[i].getState() == Thread.State.TIMED_WAITING) {
                threads[i].interrupt();
            }
        }
    }

    public void setConstant(NumberType x, NumberType y) {
        while (x.getClass() != cX.getClass()) {
            changeNumberSystem(x.isMorePrecise(cX));
        }
        cX = x;
        cY = y;
    }

    @Override
    public void changeNumberSystem(boolean up) {
        super.changeNumberSystem(up);
        if (cX != null && cY != null) {//we'll just deal with this fail somewhere else
            cX = up ? cX.toNextSystem() : cX.toPreviousSystem();
            cY = up ? cY.toNextSystem() : cY.toPreviousSystem();
        }
    }

    @Override
    public void resetWindow() {
        window = new Window(new DoubleNT(0), new DoubleNT(0),
                new DoubleNT(2), new DoubleNT(1));
    }

}
