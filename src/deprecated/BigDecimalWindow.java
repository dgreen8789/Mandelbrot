package deprecated;


import java.math.BigDecimal;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author David
 */
public class BigDecimalWindow {

    public BigDecimal xCenter;
    public BigDecimal yCenter;
    public BigDecimal xRange;
    public BigDecimal yRange;

    public BigDecimalWindow(BigDecimal xCenter, BigDecimal yCenter, BigDecimal xRange, BigDecimal yRange) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.xRange = xRange;
        this.yRange = yRange;
    }

    public BigDecimalWindow(double xCenter, double yCenter, double xRange, double yRange) {
        this.xCenter = BigDecimal.valueOf(xCenter);
        this.yCenter = BigDecimal.valueOf(yCenter);
        this.xRange = BigDecimal.valueOf(xRange);
        this.yRange = BigDecimal.valueOf(yRange);
    }

    public void zoomIn(BigDecimal xCenter, BigDecimal yCenter) {
        xRange = xRange.scaleByPowerOfTen(-1);
        yRange = yRange.scaleByPowerOfTen(-1);
        this.xCenter = xCenter;
        this.yCenter = yCenter;
    }

    public void zoomOut() {
        xRange = xRange.scaleByPowerOfTen(1);
        yRange = yRange.scaleByPowerOfTen(1);
    }

    @Override
    public String toString() {
        return "Window{" + "xCenter=" + xCenter + ", yCenter=" + yCenter + ", xRange=" + xRange + ", yRange=" + yRange + '}';
    }
    
}
