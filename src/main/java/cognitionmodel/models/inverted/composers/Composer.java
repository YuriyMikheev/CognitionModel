package cognitionmodel.models.inverted.composers;

import cognitionmodel.models.inverted.Agent;

import java.util.HashMap;
import java.util.LinkedList;

public interface Composer {
    HashMap<Object, LinkedList<Agent>> compose(HashMap<Object, LinkedList<Agent>> decomposition);
}
