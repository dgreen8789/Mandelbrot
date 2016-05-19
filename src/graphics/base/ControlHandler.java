package graphics.base;


import math.DoubleMandelbrotCalculator;
import math.DoubleWindow;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import javax.swing.event.MouseInputListener;

/**
 *
 * @author David
 */
public class ControlHandler implements MouseInputListener, KeyListener, WindowListener, FocusListener, MouseWheelListener {

    private char ZOOM_KEY = 'W';
    private char UNZOOM_KEY = 'S';
    private char LEFT_KEY = 'A';
    private char RIGHT_KEY = 'D';
    private char COLOR_KEY = 'C';
    private DoubleWindow window;
    private GUI GUI;

    private final boolean[] InputMask = new boolean[6];

    public ControlHandler(GUI gui) {
        GUI = gui;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //keyPressed(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        char c = Character.toUpperCase(e.getKeyChar());

        if (c == ZOOM_KEY) {

        }
        if (c == UNZOOM_KEY) {

        }
        if (c == LEFT_KEY) {

        }
        if (c == RIGHT_KEY) {

        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        char c = Character.toUpperCase(e.getKeyChar());
        if (c == ZOOM_KEY) {
        }
        if (c == UNZOOM_KEY) {
        }
        if (c == LEFT_KEY) {
        }
        if (c == RIGHT_KEY) {
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON1) {
            double[] coords = DoubleMandelbrotCalculator.coordinateByPoint(e.getPoint(), window);

            window.zoomIn(coords[0], coords[1]);
            InputMask[GraphicsController.WINDOW_UPDATE] = true;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            window.zoomOut();
            InputMask[GraphicsController.WINDOW_UPDATE] = true;
        }

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
    }

    public char getZOOM_KEY() {
        return ZOOM_KEY;
    }

    public char getUNZOOM_KEY() {
        return UNZOOM_KEY;
    }

    public char getLEFT_KEY() {
        return LEFT_KEY;
    }

    public char getRIGHT_KEY() {
        return RIGHT_KEY;
    }

    public boolean[] getInputMask() {
        return InputMask;
    }

    public void setWindow(DoubleWindow window) {
        this.window = window;
    }

    void forceUpdate() {
        Arrays.fill(InputMask, true);
    }

}
