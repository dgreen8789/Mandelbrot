
import graphics.base.GUI;
import math.QuadDoubleNumberType;



/**
 *
 * @author David
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        QuadDoubleNumberType one = QuadDoubleNumberType.ONE;
        System.out.println(one);
        System.out.println(one.square());
        System.out.println(one.add(QuadDoubleNumberType.TEN));
        one = one.add(QuadDoubleNumberType.TEN);
        System.out.println(one.square());
        System.out.println(one.multiply(-(1/11.0)));
    }
}
