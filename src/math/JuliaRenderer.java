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
    protected NumberType x0;
    protected NumberType y0;
    private final boolean connected;

    public JuliaRenderer(int width, int height, int numberType, NumberType x0, NumberType y0) {
        super(width, height, numberType);

        threads = new JBoxedEscape[GraphicsController.THREAD_COUNT];
        this.x0 = x0;
        this.y0 = y0;
        connected = (this.x0.mEscape(this.x0, this.y0, new HashSet<>(), MAX_ITERATIONS) == MAX_ITERATIONS);
        System.out.println("The julia set for " + x0 + " + " + y0 + "i is " + (connected ? "" : " not ") + "connected");
    }

//    @Override
//    public boolean hasBoxes() {
//        return connected;
//    }
    @Override
    protected void beginRender() {
        outPool = new Pool<>();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new JBoxedEscape(xCoords, yCoords, data, histogram);
            ((JBoxedEscape) threads[i]).setX0(x0);
            ((JBoxedEscape) threads[i]).setY0(y0);
            threads[i].setInPool(inPool);
            threads[i].setOutPool(outPool);

            threads[i].setName("Drawer thread " + i);
            threads[i].start();
            System.out.println("Thread: " + threads[i].getName() + " started");

        }
        done_calculating = false;
    }

    public void setConstant(NumberType x, NumberType y) {
        x0 = x;
        y0 = y;
    }

    @Override
    public void changeNumberSystem(Class numberType) {
        super.changeNumberSystem(numberType);
        x0 = x0 == null ? null : x0.toNextSystem();
        y0 = y0 == null ? null : y0.toNextSystem();
    }

    @Override
    public void resetWindow() {
        window = new Window(new DoubleNT(0), new DoubleNT(0),
                new DoubleNT(2), new DoubleNT(1)); 
    }

}
