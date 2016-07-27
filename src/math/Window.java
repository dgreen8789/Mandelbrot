package math;

/**
 *
 * @author David
 */
public class Window {

    private int zoomLevel = 0;
    public volatile NumberType xCenter;
    public volatile NumberType yCenter;
    public volatile NumberType xRange;
    public volatile NumberType yRange;

    public Window(NumberType xCenter, NumberType yCenter, NumberType xRange, NumberType yRange) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.xRange = xRange;
        this.yRange = yRange;
    }

    public void zoomIn(NumberType xCenter, NumberType yCenter) {
        xRange = xRange.divide(10.0);
        yRange = yRange.divide(10.0);
        this.xCenter = xCenter;
        this.yCenter = yCenter;
    zoomLevel++;
    }

    public void zoomOut() {
        xRange = xRange.multiply(10.0);
        yRange = yRange.multiply(10.0);
        zoomLevel--;
    }

    @Override
    public String toString() {
        return "Window{" + "xCenter=" + xCenter + ", yCenter=" + yCenter + ", xRange=" + xRange + ", yRange=" + yRange + '}';
    }

    public String toPresentationString() {

        return String.format("Current Window: X from %s to %s, Y from %s to %s. Magnification is 10^%sx",
                xCenter.subtract(xRange).toString(),
                xCenter.add(xRange).toString(),
                yCenter.subtract(yRange).toString(),
                yCenter.add(yRange).toString(),
                zoomLevel);
    }

    public void shiftRight(NumberType amt) {
        xCenter = xCenter.add(amt);
    }

    public void shiftLeft(NumberType amt) {
        xCenter = xCenter.subtract(amt);
    }

    public void shiftUp(NumberType amt) {
        yCenter = yCenter.subtract(amt);
    }

    public void shiftDown(NumberType amt) {
        yCenter = yCenter.add(amt);
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

}
