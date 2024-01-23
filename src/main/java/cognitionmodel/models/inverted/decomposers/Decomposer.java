package cognitionmodel.models.inverted.decomposers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.inverted.Agent;

import java.util.HashMap;
import java.util.LinkedList;

public interface  Decomposer<T extends Agent> {

    public HashMap<Object, LinkedList<T>> decompose(Tuple record, String predictingfield);


}
