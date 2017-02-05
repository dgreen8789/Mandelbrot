package main;


import graphics.base.GUI;
import math.MandelbrotRenderer;



/**
 *
 * @author David
 */
public class test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Max RAM in MB:" + Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0));
        System.out.println("Running with " + MandelbrotRenderer.MAX_ITERATIONS + " iterations maxiumum");
        GUI gui = new GUI(30, args.length > 0 ? args[0].equals("true") : false);
    }
}
