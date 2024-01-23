package cognitionmodel.models.inverted.composers;

import cognitionmodel.models.inverted.Agent;

import java.util.*;
import java.util.function.Function;

public class FilterComposer implements Composer {

    Function<Agent, Boolean> agentFilter;

    public FilterComposer(Function<Agent, Boolean> agentFilter){
        this.agentFilter = agentFilter;
    }

    @Override
    public HashMap<Object, LinkedList<Agent>> composeToAgentList(HashMap<Object, LinkedList<Agent>> decomposition){
        HashMap<Object, LinkedList<Agent>> result = new HashMap<Object, LinkedList<Agent>>();

        for (Map.Entry<Object, LinkedList<Agent>> re : decomposition.entrySet())
            result.put(re.getKey(), compose(re.getValue()));

        return result;
    }

    @Override
    public HashMap<Object, Composition> composeToBestCompositions(HashMap<Object, LinkedList<Agent>> decomposition) {
        return null;
    }

    @Override
    public HashMap<Object, List<Composition>> composeToSortedCompositions(HashMap<Object, LinkedList<Agent>> decomposition) {
        return null;
    }

    private LinkedList<Agent> compose(LinkedList<Agent> agentList){

        LinkedList<Agent> rlist = new LinkedList<>();

        for (Agent agent: agentList)
            if (agentFilter.apply(agent)) rlist.add(agent);

        return rlist.isEmpty()? agentList: rlist;
    }

}
