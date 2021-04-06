package cognitionmodel.datasets;

import java.util.ArrayList;

/**
 * Represents set of tuples
 */


public class Tuples {
    private ArrayList<Tuple> tuples = new ArrayList<>();

    public Tuples(ArrayList<Tuple> tuples) {
        this.tuples = tuples;
    }

    public ArrayList<Tuple> getTuples() {
        return tuples;
    }
}
