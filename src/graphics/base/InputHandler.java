package graphics.base;

import graphics.base.GraphicsController.GraphicsOperation;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.event.MouseInputListener;

/**
 *
 * @author David
 */
public class InputHandler implements MouseInputListener, KeyListener {

    private final char UP_KEY = 'W';
    private final char DOWN_KEY = 'S';
    private final char LEFT_KEY = 'A';
    private final char RIGHT_KEY = 'D';
    private final char COLOR_KEY = 'C';
    private final char SUPER_SAMPLE_KEY = 'Q';
    private final char BOX_KEY = 'B';

    private ArrayList<GraphicsOperation> input;
    private Point mousePoint;

    public InputHandler() {
        input = new ArrayList<>();
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
                input.add(GraphicsOperation.WINDOW_PAN_UP_UPDATE);
                break;
            case DOWN_KEY:
                input.add(GraphicsOperation.WINDOW_PAN_DOWN_UPDATE);
                break;
            case LEFT_KEY:
                input.add(GraphicsOperation.WINDOW_PAN_LEFT_UPDATE);
                break;
            case RIGHT_KEY:
                input.add(GraphicsOperation.WINDOW_PAN_RIGHT_UPDATE);
                break;
            case COLOR_KEY:
                input.add(GraphicsOperation.WINDOW_COLOR_UPDATE);
                break;
            case SUPER_SAMPLE_KEY:
                input.add(GraphicsOperation.SUPER_SAMPLE_TOGGLE);
                break;
             case BOX_KEY:
                input.add(GraphicsOperation.BOX_KEY);
                break;


        }

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
        mousePoint = e.getPoint();
        if (e.getButton() == MouseEvent.BUTTON1) {
            input.add(GraphicsOperation.WINDOW_ZOOM_IN_UPDATE);
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            input.add(GraphicsOperation.WINDOW_ZOOM_OUT_UPDATE);
        }

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public ArrayList<GraphicsOperation> getInput() {
        return input;
    }

    public Point getMousePoint() {
        return mousePoint;
    }

    void refresh() {
        input.add(GraphicsOperation.REFRESH);
    }

    void forceZoom(int x, int y) {
        mousePoint = new Point(x, y);
        input.add(GraphicsOperation.WINDOW_ZOOM_IN_UPDATE);
    }

}
