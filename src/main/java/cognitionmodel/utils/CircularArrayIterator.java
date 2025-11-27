package cognitionmodel.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CircularArrayIterator<E> implements Iterator<E> {
    private final E[] array;
    private int currentIndex = 0;
    private final int size;
    private int count = 0; // To limit iterations if needed

    public CircularArrayIterator(E[] array, int size) {
        this.array = array;
        this.size = size; // total number of elements to iterate over (could be infinite if size = Integer.MAX_VALUE)
    }

    @Override
    public boolean hasNext() {
        return count < size;
    }

    public int getCurrentIndex(){
        return currentIndex;
    }
    

    @Override
    public E next() {
        if (!hasNext()) throw new NoSuchElementException();
        E element = array[currentIndex];
        currentIndex = (currentIndex + 1) % array.length;
        count++;
        return element;
    }
}