
import graphics.base.GUI;
import math.QuadDouble;



/**
 *
 * @author David
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        QuadDouble one = QuadDouble.ONE;
        System.out.println(one);
        System.out.println(one.square());
        System.out.println(one.add(QuadDouble.TEN));
        one = one.add(QuadDouble.TEN);
        System.out.println(one.square());
        System.out.println(one.multiply(-(1/11.0)));
    }
}
