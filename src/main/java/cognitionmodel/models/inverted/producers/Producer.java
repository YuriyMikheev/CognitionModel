package cognitionmodel.models.inverted.producers;

import cognitionmodel.datasets.Tuple;
import cognitionmodel.models.inverted.composers.Composition;

import java.util.HashMap;
import java.util.List;

public interface Producer {
    public Tuple produce(HashMap<Object, List<Composition>> compositions);

}
