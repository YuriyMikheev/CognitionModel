package cognitionmodel.datasets;

import java.util.ArrayList;

/**
 * Represents set of tuples
 */


public class Tuple {
    private ArrayList<TupleElement> tupleElements = new ArrayList<>();

    public Tuple(ArrayList<TupleElement> tupleElements) {
        this.tupleElements = tupleElements;
    }

    public ArrayList<TupleElement> getTuples() {
        return tupleElements;
    }
}
