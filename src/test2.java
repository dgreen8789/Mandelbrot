
import math.QuadDouble;

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
        for (int i = 1; i < 1000; i++) {
            System.out.println(new QuadDouble(i, 0,0, Math.pow(10, -i)).square());
        }
        
    }
}


