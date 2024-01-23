package cognitionmodel.models.inverted.composers;

import cognitionmodel.models.inverted.Agent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public interface Composer {
    HashMap<Object, LinkedList<Agent>> composeToAgentList(HashMap<Object, LinkedList<Agent>> decomposition);

    HashMap<Object, Composition> composeToBestCompositions(HashMap<Object, LinkedList<Agent>> decomposition);

    HashMap<Object, List<Composition>> composeToSortedCompositions(HashMap<Object, LinkedList<Agent>> decomposition);
}
