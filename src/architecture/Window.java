package architecture;

import math.MandelbrotRenderer;
import math.numbertypes.NumberType;

/**
 *
 * @author David
 */
public class Window {

    private double zoomLevel = 1;
    public NumberType xCenter;
    public NumberType yCenter;
    public NumberType xRange;
    public NumberType yRange;
    private String presentationString;

    public Window(NumberType xCenter, NumberType yCenter, NumberType xRange, NumberType yRange) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.xRange = xRange;
        this.yRange = yRange;
        generatePresentationString();

    }

    public void zoom(NumberType xCenter, NumberType yCenter, double factor) {
        //System.out.println("zoomed in. Factor =  " + factor);
        xRange = xRange.divide(factor);
        yRange = yRange.divide(factor);
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        zoomLevel *= factor;
        generatePresentationString();
    }

    @Override
    public String toString() {
        return "Window{" + "xCenter=" + xCenter + ", yCenter=" + yCenter + ", xRange=" + xRange + ", yRange=" + yRange + '}';
    }

    private void generatePresentationString() {
        presentationString = String.format("Current Window: X from %s to %s, Y from %s to %s. Magnification is 10^%.4fx",
                xCenter.subtract(xRange).toString(),
                xCenter.add(xRange).toString(),
                yCenter.subtract(yRange).toString(),
                yCenter.add(yRange).toString(),
                getZoomLevel());
    }

    public String toPresentationString() {
        return presentationString;
    }

    public void shiftRight(NumberType amt) {
        xCenter = xCenter.add(amt);
        generatePresentationString();
    }

    public void shiftLeft(NumberType amt) {
        xCenter = xCenter.subtract(amt);
        generatePresentationString();
    }

    public void shiftUp(NumberType amt) {
        yCenter = yCenter.add(amt);
        generatePresentationString();
    }

    public void shiftDown(NumberType amt) {
        yCenter = yCenter.subtract(amt);
        generatePresentationString();
    }

    public double getZoomLevel() {
        return Math.log10(zoomLevel);
    }

    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel = Math.pow(10, zoomLevel);
        generatePresentationString();
    }

}
