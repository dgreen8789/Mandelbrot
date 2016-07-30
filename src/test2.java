
import math.QuadDoubleNumberType;

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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        QuadDoubleNumberType test = QuadDoubleNumberType.ONE;
        while (!test.equals(QuadDoubleNumberType.ZERO)) {
            test = test.divide(10);
            System.out.println(test);
        }
    }
}


