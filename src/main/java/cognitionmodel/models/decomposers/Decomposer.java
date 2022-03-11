package cognitionmodel.models.decomposers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.inverted.Agent;
import cognitionmodel.models.inverted.InvertedTabularModel;

import java.util.LinkedList;

public interface Decomposer {


    public LinkedList<Agent> decompose(Tuple record, String predictingfield);

}
