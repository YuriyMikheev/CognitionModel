package cognitionmodel.models.inverted.decomposers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.inverted.Agent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

public interface Decomposer {

    public HashMap<Object, LinkedList<Agent>> decompose(Tuple record, String predictingfield);


}
