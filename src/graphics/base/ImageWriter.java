/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphics.base;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import math.Window;

/**
 *
 * @author David
 */
public class ImageWriter {

    String path;

    public ImageWriter(String path) {
        this.path = path;
    }

    public void writeToFile(BufferedImage img, Window w) {
        long start = System.currentTimeMillis();
        File outputfile = new File(String.format("%s\\(%s, %s)(10^-%d zoom)@%d.jpg", path, w.xCenter, w.yCenter, w.getZoomLevel(), start));
        try {
            outputfile.createNewFile();
            ImageIO.write(img, "jpg", outputfile);
            System.out.print("Wrote image to " + outputfile);
        } catch (IOException ex) {
            System.out.println("IOException while writing image");        }
        long stop = System.currentTimeMillis();
        System.out.println("in " + (stop - start) + " ms");
    }
}
