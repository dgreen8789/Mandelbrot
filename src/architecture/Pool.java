/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author David
 */
public class Pool<T> {
    private final  ArrayList<T> values;
    public  Pool(){
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
        return (ArrayList<T>)values.clone();
    }
    public synchronized ArrayList<T> getBackedValues() {
        return values;
    }

    public synchronized boolean isEmpty() {
        return values.isEmpty();
    }

    public synchronized int size() {
        return values.size();
    }

    public synchronized void clear() {
        values.clear();
    }

    public synchronized void forEach(Consumer<T> action) {
        values.forEach(action);
    }
    

    public synchronized boolean removeIf(Predicate<? super T> filter) {
        return values.removeIf(filter);
    }
   

    public <T> T[] toArray(T[] a) {
        return values.toArray(a);
    }
        
}
