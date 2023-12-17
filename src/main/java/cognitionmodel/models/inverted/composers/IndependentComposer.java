package cognitionmodel.models.inverted.composers;

import cognitionmodel.models.inverted.Agent;

import java.util.*;

/**
 * Composer class that for each of the predicting values produces sets of independent agents with max MR
 */

public class IndependentComposer implements Composer {

    private int fieldsLength, predictingIndex;


    public IndependentComposer(int fieldsLength, int predictingIndex){
        this.fieldsLength = fieldsLength;
        this.predictingIndex = predictingIndex;
    }

    @Override
    public HashMap<Object, LinkedList<Agent>> compose(HashMap<Object, LinkedList<Agent>> decomposition){
        HashMap<Object, LinkedList<Agent>> result = new HashMap<Object, LinkedList<Agent>>();

        for (Map.Entry<Object, LinkedList<Agent>> re : decomposition.entrySet())
            result.put(re.getKey(), compose(re.getValue()));

        return result;
    }

    private LinkedList<Agent> compose(LinkedList<Agent> agentList){
        LinkedList<Composition> compositions = new LinkedList<>();
        PriorityQueue<Composition> bestCompositions = new PriorityQueue<>(Comparator.comparing(Composition::getMr).reversed());
        PriorityQueue<Composition> worstCompositions = new PriorityQueue<>(Comparator.comparing(Composition::getMr));

        for (Agent agent : agentList) {
            compositions.add(new Composition(agent, predictingIndex));
        }

        bestCompositions.addAll(compositions);
        worstCompositions.addAll(compositions);

        do {
            LinkedList<Composition> ncomp = new LinkedList<>();
            for (Composition composition : compositions) try {
                Composition oc = composition.clone(); boolean ncy = false;
                for (Agent agent : agentList)  {
                    oc = ncy? composition.clone() : oc;
                    if (ncy = oc.add(agent)) {
                        ncomp.add(oc);
                        bestCompositions.add(oc);
                        worstCompositions.add(oc);
                    }
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            compositions = ncomp;
        } while (!compositions.isEmpty());

        LinkedList<Agent> rlist = new LinkedList<>();
        if (!bestCompositions.isEmpty()) rlist.addAll(bestCompositions.peek().getAgents());
        if (!worstCompositions.isEmpty()) rlist.addAll(worstCompositions.peek().getAgents());

        return rlist.isEmpty()? agentList: rlist;
    }

}
