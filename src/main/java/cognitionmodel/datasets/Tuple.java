package cognitionmodel.datasets;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Set of tupleElements
 */


public class Tuple implements Iterable<TupleElement>, Serializable, Cloneable {
    private ArrayList<TupleElement> tupleElements = new ArrayList<>();

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

    public Tuple add(TupleElement tupleElement){
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
        tupleElements.add(new TupleElement(object.toString()));
        return this;
    }


    /**
     * Adds collection of objects to the end of tuple. Integer, Double, String, Char are allowed
     * @param collection - added data collection
     * @return - this object
     */

    public Tuple addAll(Collection collection){
        for (Object o: collection)
            tupleElements.add(new TupleElement(o.toString()));
        return this;
    }

    public Tuple addAll(String[] strings){
        for (String s : strings) {
            add(new TupleElement(s));
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
        int i = 0;

        for (TupleElement tupleElement: tupleElements)
            if (tupleElement.getValue().toString().equals(element.toString())) break;
                else i++;

        return i;
    }

    @Override
    public Tuple clone(){

        Tuple tuple = new Tuple();

        for (TupleElement tupleElement: tupleElements)
            tuple.add(tupleElement.getValue());

        return tuple;
    }


}
