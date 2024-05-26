package cognitionmodel.models.inverted.index;

import org.apache.commons.math3.exception.OutOfRangeException;

import java.util.*;
import java.util.function.Consumer;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class TextTokens implements Iterable<Integer>{

    private ArrayList<int[]> arrays = new ArrayList<>();
    private int last = -1;
    private int addsize = 1024 * 1024 * 16, maxsize = 1024*1024*1024;
    private long maxToken = - 1;

    public int get(long index){
        if (index < 0l || index > size())
            throw new OutOfRangeException(index, 0, size());
        return getArr(index)[(int) (index % maxsize)];
    }

    public void set(long index, int value){
        if (index < 0l || index > size()) throw new OutOfRangeException(index, 0, size());
        getArr(index)[(int) (index % maxsize)] = value;
        maxToken = max(maxToken, value);
    }

    public void add(int value){
        if (last >= maxsize || last < 0){
            last = 0;
            arrays.add(new int[addsize]);
        }

        int[] a = arrays.get(arrays.size()-1);

        if (last >= a.length) {
            a = Arrays.copyOf(a, (int) min(last + (long)addsize, maxsize));
            arrays.set(arrays.size()-1, a);
        }

        a[last++] = value;
        maxToken = max(maxToken, value);
    }

    public void addAll(Collection<Integer> integers){
        integers.forEach(this::add);
    }

    public void addAll(int[] integers){
        for (int i: integers)
            add(i);
    }


    private int[] getArr(long index){
        return arrays.get((int) (index / maxsize));
   }


    public long size(){
        return ((long)arrays.size()-1)*maxsize+(long)last;
    }


    public Iterator<Integer> iterator(long start) {
        return new Iterator<Integer>() {

            private long i = start, j = start % maxsize;
            private int[] curarr = getArr(i);
            @Override
            public boolean hasNext() {
                return i < size();
            }

            @Override
            public Integer next() {
                int x;
                try{
                    x = curarr[(int) j];
                } catch (ArrayIndexOutOfBoundsException e){
                    if ( i >= size()) return null;
                    curarr = getArr(i); j = 0;
                    x = curarr[(int)j];
                }

                i++; j++;
                return x;
            }
        };
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {

            private long i = 0, j = 0;
            private int[] curarr = getArr(i);
            @Override
            public boolean hasNext() {
                return i < size();
            }

            @Override
            public Integer next() {
                int x;
                try{
                    x = curarr[(int) j];
                } catch (ArrayIndexOutOfBoundsException e){
                    if ( i >= size()) return null;
                    curarr = getArr(i); j = 0;
                    x = curarr[(int)j];
                }

                i++; j++;
                return x;
            }
        };
    }

    public long getMaxToken() {
        return maxToken;
    }

    @Override
    public void forEach(Consumer<? super Integer> action) {

    }

    @Override
    public Spliterator<Integer> spliterator() {
        return null;
    }
}
