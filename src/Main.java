
import java.util.HashSet;
import math.numbertypes.DoubleNT;



/**
 *
 * @author David
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DoubleNT t = new DoubleNT(2);
        int x  = t.escape(t, t, new HashSet<Integer>(), 8192);
        System.out.println(x);
    }
}
