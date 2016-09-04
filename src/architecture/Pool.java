/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author David
 */
public class Pool<T> {
    private final ArrayList<T> values;
    public Pool(){
        values = new ArrayList<>();
    }
    public synchronized boolean add(T e) {
        //System.out.println(values);
        return values.add(e);
    }

    public synchronized T remove(int index) {
       
        return values.remove(index);
    }

    public synchronized boolean addAll(Collection<? extends T> c) {
        return values.addAll(c);
    }

    public synchronized ArrayList<T> getValues() {
        return values;
    }

    public synchronized boolean isEmpty() {
        return values.isEmpty();
    }

    public synchronized int size() {
        return values.size();
    }
    
        
    
        
}
