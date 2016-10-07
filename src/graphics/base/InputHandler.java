package graphics.base;

import graphics.base.GraphicsController.GraphicsOperation;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import javax.swing.event.MouseInputListener;

/**
 *
 * @author David
 */
public class InputHandler implements MouseInputListener, MouseWheelListener, KeyListener {

    private final char UP_KEY = 'W';
    private final char DOWN_KEY = 'S';
    private final char LEFT_KEY = 'A';
    private final char RIGHT_KEY = 'D';
    private final char COLOR_KEY = 'C';
    private final char SUPER_SAMPLE_TOGGLE_KEY = 'I';
    private final char DECREASE_SUPER_SAMPLE_KEY = 'O';
    private final char INCREASE_SUPER_SAMPLE_KEY = 'P';
    private final char JULIA_KEY = 'J';
    private final char BOX_KEY = 'B';
    private final char TILT_FORWARD_KEY = 'T';
    private final char TILT_BACKWARD_KEY = 'G';
    private final char REFRESH_KEY = 'R';
    private final char SAVE_TO_FILE_KEY = 'Q';

    private ArrayList<GraphicsOperation> input;
    private Point mousePoint;
    private int scrollDistance;

    public InputHandler() {
        input = new ArrayList<>();
        //input.add(GraphicsOperation.REFRESH);
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
            case SUPER_SAMPLE_TOGGLE_KEY:
                input.add(GraphicsOperation.SUPER_SAMPLE_TOGGLE);
                break;
            case DECREASE_SUPER_SAMPLE_KEY:
                input.add(GraphicsOperation.DECREASE_SUPER_SAMPLE);
                break;
            case INCREASE_SUPER_SAMPLE_KEY:
                input.add(GraphicsOperation.INCREASE_SUPER_SAMPLE);
                break;
            case JULIA_KEY:
                input.add(GraphicsOperation.JULIA_KEY);
                break;
            case BOX_KEY:
                input.add(GraphicsOperation.SHOW_BOXES);
                break;
            case TILT_FORWARD_KEY:
                input.add(GraphicsOperation.TILT_FORWARD);
                break;
            case TILT_BACKWARD_KEY:
                input.add(GraphicsOperation.TILT_BACKWARD);
                break;
            case REFRESH_KEY:
                input.add(GraphicsOperation.REFRESH);
                break;
            case SAVE_TO_FILE_KEY:
                input.add(GraphicsOperation.SAVE_TO_FILE);
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
            input.add(GraphicsOperation.WINDOW_HARD_ZOOM_IN_UPDATE);
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            input.add(GraphicsOperation.WINDOW_HARD_ZOOM_OUT_UPDATE);
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

    void forceZoom(int x, int y) {
        mousePoint = new Point(x, y);
        input.add(GraphicsOperation.WINDOW_HARD_ZOOM_IN_UPDATE);
    }

    void forceRefresh() {
        input.add(GraphicsOperation.REFRESH);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mousePoint = e.getPoint();
        scrollDistance = e.getWheelRotation();

        input.add(
                scrollDistance < 0
                        ? GraphicsOperation.WINDOW_SOFT_ZOOM_IN_UPDATE
                        : GraphicsOperation.WINDOW_SOFT_ZOOM_OUT_UPDATE);
        scrollDistance = Math.abs(scrollDistance);
    }

    public int getScrollDistance() {
        return scrollDistance;
    }

}
