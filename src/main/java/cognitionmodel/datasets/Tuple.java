package cognitionmodel.datasets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Represents set of tuples
 */


public class Tuple implements Iterable<TupleElement> {
    private ArrayList<TupleElement> tupleElements = new ArrayList<>();

    public Tuple(ArrayList<TupleElement> tupleElements) {
        this.tupleElements = tupleElements;
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
}
