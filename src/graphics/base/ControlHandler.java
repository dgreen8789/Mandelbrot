package graphics.base;

import math.MandelbrotCalculator;
import math.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;
import math.NumberType;

/**
 *
 * @author David
 */
public class ControlHandler implements MouseInputListener, KeyListener  {

    private final char UP_KEY = 'W';
    private final char DOWN_KEY = 'S';
    private final char LEFT_KEY = 'A';
    private final char RIGHT_KEY = 'D';
    private final char COLOR_KEY = 'C';
    private volatile Window window;

    private final boolean[] InputMask = new boolean[8];

    public ControlHandler(GUI gui) {
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
        switch (c) {
            case UP_KEY:
                InputMask[GraphicsController.WINDOW_PAN_UP_UPDATE] = true;
                break;
            case DOWN_KEY:
                InputMask[GraphicsController.WINDOW_PAN_DOWN_UPDATE] = true;
                break;
            case LEFT_KEY:
                InputMask[GraphicsController.WINDOW_PAN_LEFT_UPDATE] = true;
                break;
            case RIGHT_KEY:
                InputMask[GraphicsController.WINDOW_PAN_RIGHT_UPDATE] = true;
                break;
            case COLOR_KEY:
                InputMask[GraphicsController.WINDOW_COLOR_UPDATE] = true;
                break;
            
        }
        InputMask[GraphicsController.ANYTHING] = true;

    }

    @Override
    public void keyReleased(KeyEvent e) {

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
            NumberType[] coords = MandelbrotCalculator.coordinateByPoint(e.getPoint(), window);
            window.zoomIn(coords[0], coords[1]);
            InputMask[GraphicsController.WINDOW_ZOOM_UPDATE] = true;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            window.zoomOut();
            InputMask[GraphicsController.WINDOW_ZOOM_UPDATE] = true;
        }
        InputMask[GraphicsController.ANYTHING] = true;

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    public boolean[] getInputMask() {
        return InputMask;
    }

    void forceUpdate() {
        InputMask[GraphicsController.ANYTHING] = InputMask[GraphicsController.WINDOW_ZOOM_UPDATE]
                = InputMask[GraphicsController.WINDOW_COLOR_UPDATE] = true;
    }

}
