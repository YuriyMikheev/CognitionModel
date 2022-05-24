package cognitionmodel.models.inverted;

import cognitionmodel.datasets.Tuple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

public interface Decomposer {

    public HashMap<Object, LinkedList<Agent>> decompose(Tuple record, String predictingfield);

}
