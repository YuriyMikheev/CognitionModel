package cognitionmodel.datasets;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Consists data in form of set of tupleElements
 */


public class Tuple implements Iterable<TupleElement>, Serializable, Cloneable {
    private ArrayList<TupleElement> tupleElements = new ArrayList<>();
    private ConcurrentHashMap<String, int[]> indexMap = new ConcurrentHashMap<>();

    public Tuple(List<TupleElement> tupleElements) {
        this.tupleElements.addAll(tupleElements);
    }

    public Tuple() {
    }

    public ArrayList<TupleElement> getTupleElements() {
        return tupleElements;
    }

    @Override
    public Iterator<TupleElement> iterator() {
        return tupleElements.iterator();
    }

    @Override
    public void forEach(Consumer<? super TupleElement> action) {
        tupleElements.forEach(action);
    }

    @Override
    public Spliterator<TupleElement> spliterator() {
        return tupleElements.spliterator();
    }

    public int size(){
        return tupleElements.size();
    }

    public synchronized Tuple add(TupleElement tupleElement){
        String k = tupleElement.getValue().toString();
        if (indexMap.containsKey(k)) {
            int[] v = indexMap.get(k);
            v = Arrays.copyOf(v, v.length + 1);
            v[v.length - 1] = tupleElements.size();
            indexMap.put(k, v);
        }
        else
            indexMap.put(k, new int[]{tupleElements.size()});
        tupleElements.add(tupleElement);
        return this;
    }

    public TupleElement get(int index){
        return tupleElements.get(index);
    }



    /**
     * Adds object to the end of tuple. Integer, Double, String, Char are allowed
     * @param object - added data
     * @return - this object
     */

    public Tuple add(Object object){
        return add(new TupleElement(object));
    }


    /**
     * Adds collection of objects to the end of tuple. Integer, Double, String, Char are allowed
     * @param collection - added data collection
     * @return - this object
     */

    public Tuple addAll(Collection collection){
        for (Object o: collection)
            add(new TupleElement(o.toString()));
        return this;
    }

    public Tuple addAll(String[] strings){
        for (String s : strings) {
            add(s);
        }
        return this;
    }

    public Tuple addAll(double[] doubles){
        for (double d : doubles) {
            add(d);
        }
        return this;
    }

    public String toString(){
        String s = "";

        int i = 0;
        for (TupleElement tupleElement: tupleElements)
            s = s + tupleElement + (i++ < tupleElements.size() - 1 ? "," : "");

        return s;
    }

    public int findFirstIndex(Object element){

        if (indexMap.containsKey(element.toString()))
            return indexMap.get(element.toString())[0];
        else
            return -1;
    }

    public int[] findAllIndices(Object element){
        if (indexMap.containsKey(element.toString()))
            return indexMap.get(element.toString());
        else
            return new int[]{};
    }

    @Override
    public Tuple clone(){

        Tuple tuple = new Tuple();

        for (TupleElement tupleElement: tupleElements)
            tuple.add(tupleElement.getValue());

        return tuple;
    }

    /**
     * Gets elements of tuple as array of doubles
     * @return - array of doubles
     *
     * @throws ClassCastException if tuple element is not int or double
     */

    public double[] asDoubleArray(){
        double[] r = new double[size()];

        int i =0;
        for (TupleElement tupleElement: tupleElements)
            r[i++] = tupleElement.asDouble();

        return r;

    }


    /**
     * Gets elements of tuple as array of ints
     * @return - array of ints
     *
     * @throws ClassCastException if tuple element is not int or double, double elements are rounded
     */

    public int[] asIntArray(){
        int[] r = new int[size()];

        int i =0;
        for (TupleElement tupleElement: tupleElements)
            r[i++] = tupleElement.asInt();

        return r;

    }


    public TupleElement set(int index, Object object){
        return tupleElements.set(index, new TupleElement(object));
    }

}
