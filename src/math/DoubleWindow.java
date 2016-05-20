package math;


import java.math.BigDecimal;
import java.util.Arrays;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author David
 */
public class DoubleWindow {

    public double xCenter;
    public double yCenter;
    public double xRange;
    public double yRange;

    public DoubleWindow(double xCenter, double yCenter, double xRange, double yRange) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.xRange = xRange;
        this.yRange = yRange;
    }

    public void zoomIn(double xCenter, double yCenter) {
        xRange = xRange / 10.0;
        yRange = yRange / 10.0;
        this.xCenter = xCenter;
        this.yCenter = yCenter;
    }

    public void zoomOut() {
        xRange = xRange * 10.0;
        yRange = yRange * 10.0;
    }

    @Override
    public String toString() {
        return "Window{" + "xCenter=" + xCenter + ", yCenter=" + yCenter + ", xRange=" + xRange + ", yRange=" + yRange + '}';
    }
    public static DoubleWindow fromString(String str){
        String[] pieces = str.split("[=]|[, ]|[E]");
        return new DoubleWindow(
                Double.parseDouble(pieces[1]),
                Double.parseDouble(pieces[4]),
                Double.parseDouble(pieces[7]) * Math.pow(10, Integer.parseInt(pieces[8])),
                Double.parseDouble(pieces[11]) * Math.pow(10, Integer.parseInt(pieces[12].substring(0, 2)))
        );
    }
    public void shiftRight(double amt){
        xCenter += amt;
    }
    public void shiftLeft(double amt){
        xCenter -= amt;
    }
     public void shiftUp(double amt){
        yCenter += amt;
    }
     public void shiftDown(double amt){
         yCenter -= amt;
     }



}
