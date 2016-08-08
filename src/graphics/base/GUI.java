package graphics.base;

import math.Window;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import math.DoubleNT;

public class GUI extends Thread {

    private boolean isRunning = true;
    private final Canvas canvas;
    private BufferStrategy strategy;
    private BufferedImage background;
    private Graphics2D backgroundGraphics;
    private Graphics2D graphics;
    private final JFrame frame;
    private int width = 432;
    private int height = 288;
    private final AtomicInteger FPS = new AtomicInteger();
    private final AtomicInteger FPSLimit;
    private final GraphicsConfiguration config
            = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();
    private final FPSCounter fpsCounter = new FPSCounter(FPS);
    public final GraphicsController graphicsControl;
    public final ControlHandler controlHandler;


    // create a hardware accelerated image
    public final BufferedImage create(final int width, final int height, final boolean alpha) {
        return config.createCompatibleImage(width, height, alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
    }

    // Setup
    public GUI(int FPSLimit, boolean FULL_SCREEN) {
        // JFrame
        frame = new JFrame();
        frame.addWindowListener(new FrameClose());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setSize(width, height);
        if (FULL_SCREEN) {
            boolean b = setFullScreen(frame);
            //System.out.println(frame.getWidth());
            width = frame.getWidth();
            height = frame.getHeight();
        }
        frame.setResizable(false);
        frame.setVisible(true);

        // Canvas
        canvas = new Canvas(config);
        canvas.setSize(width, height);
        // System.out.println(canvas.getSize());
        frame.add(canvas, 0);

        // Background & Buffer
        background = create(width, height, true);
        canvas.createBufferStrategy(2);
        do {
            strategy = canvas.getBufferStrategy();
        } while (strategy == null);

        //Initialize Graphics
        graphicsControl = new GraphicsController(width, height, frame.getInsets());

        //Initialize input listeners
        this.controlHandler = new ControlHandler(this);
        addListeners();

        //Initialize FPS Counter
        this.FPSLimit = new AtomicInteger(FPSLimit);
        Timer fpsTimer = new Timer();
        fpsTimer.schedule(fpsCounter, 0, 1000);

        //LIFTOFF *rocket noises*
        start();
    }

    private void addListeners() {
        canvas.addMouseMotionListener(controlHandler);
        canvas.addKeyListener(controlHandler);
        canvas.addMouseListener(controlHandler);
    }

    public boolean setFullScreen(JFrame frame) {
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
        Dimension x = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(x);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        return true;
    }

    private class FrameClose extends WindowAdapter {

        @Override
        public void windowClosing(final WindowEvent e) {
            isRunning = false;
        }
    }

    // Screen and buffer stuff
    private Graphics2D getBuffer() {
        if (graphics == null) {
            try {
                graphics = (Graphics2D) strategy.getDrawGraphics();
            } catch (IllegalStateException e) {
                return null;
            }
        }
        return graphics;
    }

    private boolean updateScreen() {
        graphics.dispose();
        graphics = null;
        try {
            strategy.show();
            Toolkit.getDefaultToolkit().sync();
            return (!strategy.contentsLost());

        } catch (NullPointerException e) {
            return true;

        } catch (IllegalStateException e) {
            return true;
        }
    }

    public void run() {
        //String str ="Window{xCenter=0.08039514558823516, yCenter=0.6253787604166666, xRange=1.7499999999999997E-7, yRange=1.0000000000000002E-7}";
        Window window
                = //DoubleWindow.fromString(str);
                new Window(new DoubleNT(-.75),
                        new DoubleNT(0),
                        new DoubleNT(1.75),
                        new DoubleNT(1));
        graphicsControl.setWindow(window);
        controlHandler.setWindow(window);
        controlHandler.forceUpdate();
        main:
        while (isRunning) {
            long fpsWait = (long) (1.0 / FPSLimit.get() * 1000);
            width = frame.getWidth();
            height = frame.getHeight();

            canvas.setSize(width, height);
            background = create(width, height, true);
            backgroundGraphics = (Graphics2D) background.getGraphics();
            //System.out.println(width + " " + height);
            long renderStart = System.nanoTime();
            frame.setTitle("Night of No Limits Mandelbrot Fractal Generator running at: " + FPS.intValue() + " FPS");
            // Update Graphics

            do {
                Graphics2D bg = getBuffer();
                if (!isRunning) {
                    break main;
                }
                renderApplication(backgroundGraphics, canvas.getWidth(), canvas.getHeight()); // this calls your draw method
                // thingy
                bg.drawImage(background, 0, 0, null);

                bg.dispose();
            } while (!updateScreen());
            fpsCounter.incrementFPSCount();
            // Better do some FPS limiting here
            long renderTime = (System.nanoTime() - renderStart) / 1000000;
            try {
                Thread.sleep(Math.max(0, fpsWait - renderTime));
            } catch (InterruptedException e) {
            }

        }
        frame.dispose();
    }

    public void renderApplication(Graphics2D g, int width, int height) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        graphicsControl.render(g, controlHandler.getInputMask());

    }

}
