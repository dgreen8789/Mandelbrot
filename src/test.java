
import math.DoubleWindow;
import java.awt.Color;
import sun.misc.Unsafe;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author David
 */
public class test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String str = "Window{xCenter=0.08039514558823516, yCenter=0.6253787604166666, xRange=1.7499999999999997E-7, yRange=1.0000000000000002E-7}";
        System.out.println(str);
        System.out.println(DoubleWindow.fromString(str));
    }
}
