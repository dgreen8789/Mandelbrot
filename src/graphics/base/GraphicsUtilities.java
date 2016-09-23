package graphics.base;



import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author David
 */
public class GraphicsUtilities {
    /**
     *
     * @param string
     * @param g
     * @param width
     * @param height
     * @return A font that will fill the given area with the given string, or
     * null if the area is too small
     */
    public static Font fillRect(String string, Graphics2D g, int width, int height) {
        Font f = g.getFont();
        Rectangle2D bounds = f.getStringBounds(string, g.getFontRenderContext());

        while (bounds.getWidth() < width || bounds.getHeight() < height) {
            f = f.deriveFont(f.getSize2D() + 1);
            bounds = f.getStringBounds(string, g.getFontRenderContext());
        }

        while (bounds.getWidth() > width || bounds.getHeight() > height) {
            if (f.getSize2D() < 2) {
                return null;
            } else {
                f = f.deriveFont(f.getSize2D() - 1);
                bounds = f.getStringBounds(string, g.getFontRenderContext());
            }
        }

        return f;
    }


}
