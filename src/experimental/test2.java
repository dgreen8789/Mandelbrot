package experimental;

import graphics.base.GraphicsController;
import graphics.base.ImageWriter;
import graphics.colors.ColorScheme;
import java.io.File;
import math.JuliaRenderer;
import math.numbertypes.DoubleNT;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author David
 */
public class test2 {

    private static final String IMAGE_PATH = "C:\\Mandelbrot Image Logs\\julia6";
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
                    System.out.println((int)(Math.random() * 300));
        }
    }
    /**
     * @param args the command line arguments
     */
//    public static void main(String[] args) {
//        String paths[] = new String[]{IMAGE_PATH, IMAGE_PATH + "_Color1", IMAGE_PATH + "_color2"};
//        for (String path : paths) {
//            check(new File(path));
//        }
//        long start = System.currentTimeMillis();
//        double k = .51;
//        int len = 100; // fps * s
//        DoubleNT[][] coords = new DoubleNT[len][2];
//        for (double i = 0; i < len; i++) {
//            double t = (i / len) * (2 * Math.PI);
//            coords[(int) i][0] = new DoubleNT(k * Math.cos(t) - (k / 2.0) * Math.cos(2 * t));
//            coords[(int) i][1] = new DoubleNT(k * Math.sin(t) - (k / 2.0) * Math.sin(2 * t));
//        }
//        int width = 160;//3840;
//        int height = 90;//2160;
//        ColorScheme[] arr = ColorScheme.values();
//        ImageWriter[] writers = new ImageWriter[]{new ImageWriter(paths[0], width, height),
//            new ImageWriter(paths[1], width, height), new ImageWriter(paths[2], width, height)};
//
//        JuliaRenderer render = new JuliaRenderer(width, height, coords[0][0], coords[0][1]);
//        render.resetWindow();
//        for (int i = 0; i < len; i++) {
//            render.setConstant(coords[i][0], coords[i][1]);
//            render.hardResetData();
//            render.getHistogram().reset();
//            render.draw();
//            do {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                }
//            } while (render.propPixelsRendered() != 1);
//            for (int j = 0; j < 3; j++) {
//                writers[j].writeToFile(render.createImage(true, arr[j], GraphicsController.THETA_MAX), render.getWindow(), true);
//            }
//            
//        }
//        long stop = System.currentTimeMillis();
//        System.out.println((stop - start) + " ms");
//
//    }
//
//    public static void check(File f) {
//        if (!f.exists()) {
//            f.mkdir();
//        } else if (!f.isDirectory()) {
//            System.exit(0);
//        }
//    }
}
