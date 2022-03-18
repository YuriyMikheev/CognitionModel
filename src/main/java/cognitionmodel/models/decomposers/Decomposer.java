package cognitionmodel.models.decomposers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.inverted.BitAgent;

import java.util.LinkedList;

public interface Decomposer {


    public LinkedList<BitAgent> decompose(Tuple record, String predictingfield);

}
